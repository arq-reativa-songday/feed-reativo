package br.ufrn.imd.feed.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.dto.SongDto;
import br.ufrn.imd.feed.exception.NotFoundException;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import br.ufrn.imd.feed.model.Feed;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
    @Autowired
    private SongDayClient songDayClient;

    @Autowired
    private SongsClient songsClient;

    public Mono<Feed> generateFeed(String username, Date lastFeedDate, int offset, int limit) {
        // buscar pessoas que o usuário segue
        Mono<Set<String>> followeesMono = this.findFollowees(username);

        Mono<Long> songs = songsClient.count()
                .next()
                .onErrorResume(throwable -> {
                    return Mono.just(0L);
                });

        return followeesMono.zipWith(songs).flatMap(t -> {
            Set<String> followees = t.getT1();
            Long songsCount = t.getT2();

            Date updatedAtOrigin = new Date();
            // buscar posts para o feed
            Mono<List<PostDto>> postsList = this.findPosts(new SearchPostsDto(offset, limit, followees)).collectList();

            return postsList.flatMap(posts -> {
                Date updatedAt = updatedAtOrigin;
                if (posts.size() > 0) {
                    Date mostRecentPostDate = posts.get(0).getCreatedAt();
                    updatedAt = new Date(mostRecentPostDate.getTime() + 1);
                }

                if (lastFeedDate != null) {
                    return findPostsCount(new SearchPostsCountDto(lastFeedDate, updatedAt, followees))
                            .zipWith(Mono.just(updatedAt)).flatMap(tuple -> {
                                Long newsPosts = tuple.getT1();
                                Date date = tuple.getT2();

                                return Mono.just(
                                        Feed.builder()
                                                .username(username)
                                                .updatedAt(date)
                                                .posts(posts)
                                                .offset(offset)
                                                .limit(limit)
                                                .size(posts.size())
                                                .newsPosts(newsPosts)
                                                .totalSongs(songsCount)
                                                .build());
                            });
                }

                return Mono.just(
                        Feed.builder()
                                .username(username)
                                .updatedAt(updatedAt)
                                .posts(posts)
                                .offset(offset)
                                .limit(limit)
                                .size(posts.size())
                                .newsPosts(null)
                                .totalSongs(songsCount)
                                .build());
            });
        });
    }

    private Flux<PostDto> findPosts(SearchPostsDto searchPostsDto) {
        return songDayClient.getAll(Mono.just(searchPostsDto))
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.empty();
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar as publicações: "
                                    + throwable.getLocalizedMessage()));
                });
    }

    private Mono<Long> findPostsCount(SearchPostsCountDto searchPostsCountDto) {
        return songDayClient.searchPostsCount(Mono.just(searchPostsCountDto))
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar a quantidade de novas publicações: "
                                    + throwable.getLocalizedMessage()));
                })
                .next();
    }

    private Mono<Set<String>> findFollowees(String username) {
        return songDayClient.getFolloweesByUsername(username)
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.error(new NotFoundException("Usuário não encontrado"));
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar os usuários seguidos: "
                                    + throwable.getLocalizedMessage()));
                })
                .next();
    }

    private Mono<SongDto> findSongById(String id) {
        return songsClient.findById(id)
                .next()
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.empty();
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música: "
                                    + throwable.getLocalizedMessage()));
                });
    }

    public Flux<PostDto> findFeedPosts(String username, int offset, int limit) {
        // buscar pessoas que o usuário segue
        return this.findFollowees(username)
                .flatMapMany(followees -> {
                    // buscar posts para o feed
                    Flux<PostDto> posts = this.findPosts(new SearchPostsDto(offset, limit, followees));

                    return posts.flatMap(post -> {
                        // para cada post, buscar os dados da música
                        return this.findSongById(post.getSongId())
                                .flatMap(song -> {
                                    if (song != null && song.getId() != null)
                                        post.setSong(song);
                                    return Mono.just(post);
                                });
                    });
                });
    }

    // Essa solução não está funcionando.
    // Além disso, quando existir posts com a mesma música, o `Flux<SongDto> songs`
    // não possuirá repetição e isso seria um problema pro zipWith.
    // public Flux<PostDto> findFeedPosts(String username, int offset, int limit) {
    //     // buscar pessoas que o usuário segue
    //     return this.findFollowees(username)
    //             .flatMapMany(followees -> {
    //                 // buscar posts para o feed
    //                 Flux<PostDto> posts = this.findPosts(new SearchPostsDto(offset, limit, followees)).cache();

    //                 // buscar músicas de cada post
    //                 Flux<SongDto> songs = songsClient.findAllById(posts.map(PostDto::getSongId));

    //                 return posts.zipWith(songs).flatMap(tuple -> {
    //                     PostDto post = tuple.getT1();
    //                     SongDto song = tuple.getT2();
    //                     if (song != null && song.getId() != null)
    //                         post.setSong(song);
    //                     return Mono.just(post);
    //                 });
    //             });
    // }
}
