package com.muggle.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BookDTO {
    private String author;
    private int page;
}
