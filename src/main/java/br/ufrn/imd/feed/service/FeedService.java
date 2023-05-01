package br.ufrn.imd.feed.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.FeedPostsDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import br.ufrn.imd.feed.model.Feed;

@Service
public class FeedService {
    @Autowired
    private SongDayClient songDayClient;

    public Feed generateFeed(String username, Date feedDate, int offset, int limit) {
        // buscar pessoas que o usuário segue
        List<UUID> followees = this.findFollowees(username);

        // montar filtro dos posts
        SearchPostsDto searchPostsDto = SearchPostsDto.builder()
                .feedDate(feedDate)
                .offset(offset)
                .limit(limit)
                .followees(followees)
                .build();
        // buscar posts para o feed
        FeedPostsDto feedPosts = this.findFeedPosts(searchPostsDto);

        List<PostDto> posts = feedPosts.getPosts();
        // montar feed
        return Feed.builder()
                .username(username)
                .updatedAt(feedPosts.getUpdatedAt())
                .posts(posts)
                .offset(offset)
                .limit(limit)
                .size(posts.size())
                .newsPosts(feedPosts.getNewsPosts())
                .build();
    }

    private FeedPostsDto findFeedPosts(SearchPostsDto searchPostsDto) {
        // TODO
        try {
            ResponseEntity<FeedPostsDto> response = songDayClient.findFeedPosts(searchPostsDto);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return new FeedPostsDto();
            }

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            // throw new ServicesCommunicationException("Error while communicating with SongDay to retrieve feed posts.");
            return new FeedPostsDto();
        }
    }

    private List<UUID> findFollowees(String username) {
        // TODO
        try {
            ResponseEntity<List<UUID>> response = songDayClient.findFollowees(username);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return Collections.emptyList();
            }

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            // throw new ServicesCommunicationException("Error while communicating with SongDay to retrieve followees.");
            return Collections.emptyList();
        }
    }
}
