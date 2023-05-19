package br.ufrn.imd.feed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${songday.api.address}")
    private String baseUrl;

    @Value("${songs.api.address}")
    private String baseUrlSongs;

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    WebClient webClientSongs() {
        return WebClient.builder()
                .baseUrl(baseUrlSongs)
                .build();
    }
}
