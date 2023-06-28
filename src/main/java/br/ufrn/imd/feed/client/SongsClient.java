package br.ufrn.imd.feed.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import reactor.core.publisher.Flux;

@HttpExchange("songs-reactive")
public interface SongsClient {
    @GetExchange(value = "/songs/count")
    public Flux<Long> count();
}
