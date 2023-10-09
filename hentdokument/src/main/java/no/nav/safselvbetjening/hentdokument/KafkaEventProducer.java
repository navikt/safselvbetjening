package no.nav.safselvbetjening.hentdokument;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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
	private static final String KAFKA_INSTANCE = "kafka";
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
					   SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.kafkaTemplate = kafkaTemplate;
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
	}

	@Retry(name = KAFKA_INSTANCE)
	@CircuitBreaker(name = KAFKA_INSTANCE)
	public void publish(HoveddokumentLest event) {

		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
				safSelvbetjeningProperties.getTopics().getDokdistdittnav(),
				null,
				System.currentTimeMillis(),
				getCallId(),
				event
		);

		try {
			SendResult<String, Object> sendResult = kafkaTemplate.send(producerRecord).get();
			log.info("hentdokument HoveddokumentLest(journalpostId={}, dokumentInfoId={}) av bruker hendelse skrevet til topic. hendelseMetadata={}",
					event.getJournalpostId(), event.getDokumentInfoId(),
					sendResult.getRecordMetadata()
			);
		} catch (ExecutionException executionException) {
			if (executionException.getCause() instanceof KafkaProducerException kafkaProducerException) {
				if (kafkaProducerException.getCause() instanceof TopicAuthorizationException) {
					throw new KafkaTechnicalException(KAFKA_NOT_AUTHENTICATED + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), kafkaProducerException.getCause());
				}
			}
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), executionException);
		} catch (Exception e) {
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + safSelvbetjeningProperties.getTopics().getDokdistdittnav(), e);
		}
	}

}
