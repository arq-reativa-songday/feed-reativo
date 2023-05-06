package br.ufrn.imd.feed.dto;

import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPostsCountDto {
    private Date start;
    private Date end;
    private Set<String> followees;
}
