package br.ufrn.imd.feed.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchPostsDto {
    private Date feedDate;
    private int offset;
    private int limit;
    private List<UUID> followees;
}
