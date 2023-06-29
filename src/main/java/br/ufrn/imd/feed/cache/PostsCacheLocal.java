package br.ufrn.imd.feed.cache;

import java.time.Duration;
import java.util.List;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.service.FeedService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostsCacheLocal {
    @Autowired
    private FeedService feedService;
    private RLocalCachedMapReactive<String, List<PostDto>> map;
    private final Duration timeToLive = Duration.ofSeconds(30);

    public PostsCacheLocal(RedissonReactiveClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, List.class);
        LocalCachedMapOptions<String, List<PostDto>> options = LocalCachedMapOptions.<String, List<PostDto>>defaults()
                // .timeToLive(30, TimeUnit.SECONDS)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.map = redissonClient.getLocalCachedMap("/posts-reativo-local/", codec, options);
    }

    public Flux<PostDto> get(String username, int offset, int limit) {
        String key = getKey(username, offset, limit);

        return getFromCache(key)
                .switchIfEmpty(generateFeed(username, offset, limit))
                .doFirst(() -> {
                    System.out.println("Obtendo chave " + key + "...");
                });
    }

    private Flux<PostDto> generateFeed(String username, int offset, int limit) {
        Flux<PostDto> posts = feedService.findFeedPosts(username, offset, limit).cache();
        String key = getKey(username, offset, limit);

        return posts
                .doOnComplete(() -> updateCache(key, posts).subscribe())
                .doFirst(() -> {
                    System.out.println("Gerando novo feed para chave " + key + "...");
                });
    }

    private Flux<PostDto> getFromCache(String key) {
        return map.get(key)
                .flatMapMany(Flux::fromIterable)
                .doFirst(() -> {
                    System.out.println("Buscando chave " + key + " no cache (local)...");
                });
    }

    private Mono<Long> updateCache(String key, Flux<PostDto> postsCache) {
        Mono<Long> expireOperation = Mono.delay(timeToLive)
                .then(Mono.defer(() -> map.fastRemove(key)));

        return postsCache.collectList()
                .flatMap(posts -> map.fastPut(key, posts))
                .then(expireOperation)
                .doFirst(() -> {
                    System.out.println("Atualizando chave " + key + " no cache (local)");
                });
    }

    private String getKey(String username, int offset, int limit) {
        return String.format("%s-offset:%s_limit:%s", username, offset, limit);
    }
}
