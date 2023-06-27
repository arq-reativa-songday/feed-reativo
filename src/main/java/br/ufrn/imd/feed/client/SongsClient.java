package br.ufrn.imd.feed.client;

import org.springframework.web.service.annotation.GetExchange;

import reactor.core.publisher.Flux;

public interface SongsClient {
    @GetExchange(value = "/songs/count")
    public Flux<Long> count();
}
