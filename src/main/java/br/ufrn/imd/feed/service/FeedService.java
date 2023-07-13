package br.ufrn.imd.feed.service;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ufrn.imd.feed.dto.GenerateFeedDto;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SongDto;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Configuration
public class FeedService {
    @Bean
    public Function<Flux<GenerateFeedDto>, Flux<GenerateFeedDto>> retrieveFeedPosts() {
        // TODO talvez verificar cache e obter dados de lá? Se não encontra, buscar e
        // salva no cache
        return input -> {
            return input.doOnNext(
                    dto -> System.out.println("\n\n\n### Atualizar feed de " + dto.getUsername() + "...\n\n\n"));
        };
    }

    @Bean
    public Function<Flux<PostDto>, Tuple2<Flux<PostDto>, Flux<String>>> retrievePostsSongs() {
        return posts -> {
            Flux<String> songsIds = posts.map(PostDto::getSongId).doOnNext(id -> {
                System.out.println("# id de música a ser buscado: " + id);
            });
            return Tuples.of(posts, songsIds);
        };
    }

    @Bean
    public Function<Tuple2<Flux<PostDto>, Flux<SongDto>>, Flux<PostDto>> buildFeed() {
        return tuple -> {
            Flux<PostDto> posts = tuple.getT1();
            Flux<SongDto> songs = tuple.getT2();

            // TODO não está funcionando como esperado
            return posts.join(songs,
                    post -> Flux.just(post.getSongId()),
                    song -> Flux.never(),
                    (post, song) -> {
                        post.setSong(song);
                        return post;
                    }).doOnNext(p -> {
                        System.out.println("# post com música! id: " + p.getId());
                    });
        };
    }

    @Bean
    public Consumer<Flux<PostDto>> printFeedPosts() {
        return posts -> {
            posts.doOnNext(post -> {
                String text;
                if (post.getSong() != null) {
                    text = String.format("\n#%s# Publicado por %s: %s - %s.", post.getCreatedAt().toString(),
                            post.getUsername(), post.getSong().getName(), post.getSong().getArtist());
                } else {
                    text = String.format("\n#%s# Publicado por %s: música não encontrada.",
                            post.getCreatedAt().toString(), post.getUsername());
                }
                System.out.println(text);
            }).subscribe();
        };
    }
}
