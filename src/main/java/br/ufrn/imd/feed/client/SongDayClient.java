package br.ufrn.imd.feed.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.ufrn.imd.feed.dto.FeedPostsDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "songday-service", url = "${songday.api.address}")
public interface SongDayClient {
    @GetMapping(value = "/users/{username}/followees")
    ResponseEntity<List<UUID>> findFollowees(@PathVariable String username);

    @PostMapping(value = "/posts/feed")
    ResponseEntity<FeedPostsDto> findFeedPosts(@RequestBody SearchPostsDto searchPostsDto);
}
