package br.ufrn.imd.feed.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.model.Feed;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
    @Autowired
    private SongDayClient songDayClient;

    public Mono<Feed> generateFeed(String username, Date lastFeedDate, int offset, int limit) {
        // buscar pessoas que o usuário segue
        Mono<Set<String>> followeesMono = this.findFollowees(username);

        return followeesMono.flatMap(followees -> {
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
                                .build());
            });
        });
    }

    private Flux<PostDto> findPosts(SearchPostsDto searchPostsDto) {
        return songDayClient.findPosts(searchPostsDto);
    }

    private Mono<Long> findPostsCount(SearchPostsCountDto searchPostsCountDto) {
        return songDayClient.findPostsCount(searchPostsCountDto);
    }

    private Mono<Set<String>> findFollowees(String username) {
        return songDayClient.findFollowees(username);
    }
}
