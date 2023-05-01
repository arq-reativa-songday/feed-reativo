package br.ufrn.imd.feed.dto;

import java.util.Date;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto {
    private UUID id;
    private Date createdAt;
    private String username;
    private UUID songId;
    private String text;
    private int likesCount;
    private int commentsCount;
}
