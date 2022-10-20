package no.nav.safselvbetjening.endpoints;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static no.nav.safselvbetjening.cache.CacheConfig.GRAPHQL_QUERY_CACHE;

@Configuration
@Profile("itest")
public class LokalCacheTestConfig {
	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache(GRAPHQL_QUERY_CACHE, Caffeine.newBuilder()
						.maximumSize(10)
						.build())));
		return manager;
	}
}
