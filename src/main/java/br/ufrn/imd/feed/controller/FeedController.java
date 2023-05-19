package br.ufrn.imd.feed.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.feed.model.Feed;
import br.ufrn.imd.feed.service.FeedService;
import reactor.core.publisher.Mono;

@RestController
public class FeedController {
    @Autowired
    private FeedService feedService;

    @GetMapping(value = "/{username}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Feed>> getFeed(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date lastFeedDate) {
        Mono<Feed> feed = feedService.generateFeed(username, lastFeedDate, offset, limit);
        return feed.map(ResponseEntity::ok);
    }
}
