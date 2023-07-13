package br.ufrn.imd.feed.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Component;

import br.ufrn.imd.feed.dto.PostDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostsCacheRemoto {
    private RMapCacheReactive<String, List<PostDto>> map;

    public PostsCacheRemoto(RedissonReactiveClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, List.class);
        this.map = redissonClient.getMapCache("/posts-reativo-remoto/", codec);
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
        // Flux<PostDto> posts = feedService.findFeedPosts(username, offset, limit).cache();
        Flux<PostDto> posts = Flux.empty();
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
                    System.out.println("Buscando chave " + key + " no cache (remoto)...");
                });
    }

    private Mono<Boolean> updateCache(String key, Flux<PostDto> postsCache) {
        return postsCache.collectList()
                .flatMap(posts -> map.fastPut(key, posts, 30, TimeUnit.SECONDS))
                .doFirst(() -> {
                    System.out.println("Atualizando chave " + key + " no cache (remoto)");
                });
    }

    private String getKey(String username, int offset, int limit) {
        return String.format("%s-offset:%s_limit:%s", username, offset, limit);
    }
}
