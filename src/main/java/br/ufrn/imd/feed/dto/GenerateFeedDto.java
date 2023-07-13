package br.ufrn.imd.feed.dto;

import lombok.Getter;

@Getter
public class GenerateFeedDto {
    private String username;
    private int offset;
    private int limit;
}
