package com.davirguezc.com.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private Long id;
    private String title;
    private String publicationTimestamp;
    private Long pages;
    private String summary;
    private Author author;
    private Long wordCount;

}
