package no.nav.safselvbetjening.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import no.nav.safselvbetjening.tokendings.TokenResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Profile({"nais", "local"})
public class CacheConfig {

	public static final String GRAPHQL_QUERY_CACHE = "graphql_query_cache";
	public static final String TOKENDINGS_CACHE = "tokendings_cache";

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache(GRAPHQL_QUERY_CACHE, Caffeine.newBuilder()
						.maximumSize(1_000)
						.recordStats()
						.build()),
				new CaffeineCache(TOKENDINGS_CACHE, Caffeine.newBuilder()
						.maximumSize(500)
						.recordStats()
						.expireAfter(new Expiry<>() {
							@Override
							public long expireAfterCreate(Object key, Object value, long currentTime) {
								if (value instanceof TokenResponse tokenResponse) {
									return TimeUnit.SECONDS.toNanos(tokenResponse.expiresIn());
								}
								return 0;
							}

							@Override
							public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
								return expireAfterCreate(key, value, currentTime);
							}

							@Override
							public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
								return currentDuration;
							}
						})
						.build())));
		return manager;
	}
}
