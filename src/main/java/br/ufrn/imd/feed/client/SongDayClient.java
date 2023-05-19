package br.ufrn.imd.feed.client;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.exception.NotFoundException;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SongDayClient {
    @Autowired
    private WebClient webClient;

    public Mono<Set<String>> findFollowees(String username) {
        return webClient
                .get()
                .uri("/users/username/"+username+"/followees")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Set<String>>() {}).next()
                .switchIfEmpty(Mono.error(new NotFoundException("O usuário não está seguindo ninguém")))
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar publicações"));
                });
    }

    public Flux<PostDto> findPosts(SearchPostsDto search) {
        return webClient
                .post()
                .uri("/posts/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(search)
                .retrieve()
                .bodyToFlux(PostDto.class)
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.empty();
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay a quantidade de novas publicações"));
                });
    }

    public Mono<Long> findPostsCount(SearchPostsCountDto search) {
        return webClient
                .post()
                .uri("/posts/search/count")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(search)
                .retrieve()
                .bodyToFlux(Long.class).next()
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay a quantidade de novas publicações"));
                });
    }
}
