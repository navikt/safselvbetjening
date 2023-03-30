package no.nav.safselvbetjening.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ExecutionException;

import static no.nav.safselvbetjening.MDCUtils.getCallId;

@Slf4j
@Component
@EnableTransactionManagement
public class KafkaEventProducer {

	private static final String KAFKA_NOT_AUTHENTICATED = "Not authenticated to publish to topic: ";
	private static final String KAFKA_FAILED_TO_SEND = "Failed to send message to kafka. Topic: ";
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
					   SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.kafkaTemplate = kafkaTemplate;
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
	}

	@Retryable(backoff = @Backoff(delay = 500))
	void publish(Object event) {

		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
				safSelvbetjeningProperties.getTopics().getDokdistdittnav(),
				null,
				System.currentTimeMillis(),
				getCallId(),
				event
		);

		try {
			SendResult<String, Object> sendResult = kafkaTemplate.send(producerRecord).get();
			log.info("Lest av bruker hendelse skrevet til topic. Timestamp={}, partition={}, offset={}, topic={}",
					sendResult.getRecordMetadata().timestamp(),
					sendResult.getRecordMetadata().partition(),
					sendResult.getRecordMetadata().offset(),
					sendResult.getRecordMetadata().topic()
			);
		} catch (ExecutionException executionException) {
			if (executionException.getCause() instanceof KafkaProducerException) {
				KafkaProducerException kafkaProducerException = (KafkaProducerException) executionException.getCause();
				if (kafkaProducerException.getCause() instanceof TopicAuthorizationException) {
					throw new KafkaTechnicalException(KAFKA_NOT_AUTHENTICATED + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), kafkaProducerException.getCause());
				}
			}
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), executionException);
		} catch (InterruptedException | KafkaException e) {
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), e);
		}
	}

}
