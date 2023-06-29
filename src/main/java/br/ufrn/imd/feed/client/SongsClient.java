package br.ufrn.imd.feed.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import br.ufrn.imd.feed.dto.SongDto;
import reactor.core.publisher.Flux;

@HttpExchange("songs-reactive")
public interface SongsClient {
    @GetExchange(value = "/songs/count")
    public Flux<Long> count();

    @GetExchange(value = "/songs/{id}")
    public Flux<SongDto> findById(@PathVariable String id);
}
