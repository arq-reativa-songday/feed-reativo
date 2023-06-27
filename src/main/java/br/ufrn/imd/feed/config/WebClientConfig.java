package br.ufrn.imd.feed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Value("${songday.api.address}")
    private String baseUrlSongDay;

    @Value("${songs.api.address}")
    private String baseUrlSongs;

    @Bean
    SongDayClient songDayClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrlSongDay)
                .defaultStatusHandler(
                        httpStatusCode -> HttpStatus.NOT_FOUND == httpStatusCode,
                        response -> Mono.empty())
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServicesCommunicationException(
                                "Erro durante a comunicação com SongDay: " + response.toString())))
                .build();

        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build()
                .createClient(SongDayClient.class);
    }

    @Bean
    SongsClient songsClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrlSongs)
                .defaultStatusHandler(
                        httpStatusCode -> HttpStatus.NOT_FOUND == httpStatusCode,
                        response -> Mono.empty())
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServicesCommunicationException(
                                "Erro durante a comunicação com Songs: " + response.toString())))
                .build();

        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build()
                .createClient(SongsClient.class);
    }
}
