package br.ufrn.imd.feed.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Value("${spring.data.redis.url}")
    private String redisAddress;

    @Bean
    RedissonReactiveClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisAddress);
        return Redisson.create(config).reactive();
    }
}
