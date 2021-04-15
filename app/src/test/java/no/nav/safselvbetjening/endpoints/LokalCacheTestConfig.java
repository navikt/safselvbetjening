package no.nav.safselvbetjening.endpoints;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static no.nav.safselvbetjening.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Configuration
@Profile("itest")
public class LokalCacheTestConfig {
	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Collections.singletonList(
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build())));
		return manager;
	}
}
