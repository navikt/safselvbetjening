package no.nav.safselvbetjening.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
@Profile({"nais", "local"})
public class CacheConfig {
	public static final String GRAPHQL_QUERY_CACHE = "graphql_query_cache";
	public static final String AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE = "RESTSTS";

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(1)
						.build()),
				new CaffeineCache(GRAPHQL_QUERY_CACHE, Caffeine.newBuilder()
						.maximumSize(1_000)
						.recordStats()
						.build())
		));
		return manager;
	}
}
