package br.ufrn.imd.feed.dto;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedPostsDto {
    private Date updatedAt;
    private int newsPosts;
    private List<PostDto> posts;

    public FeedPostsDto() {
        posts = Collections.emptyList();
    }
}
