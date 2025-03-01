package com.davirguez.model;

import com.davirguezc.com.model.Author;
import com.davirguezc.com.model.Book;

public class BookFixture {
    public static Book buildBook(Long id, String title, String name, Long pages) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(Author.builder().name(name).build())
                .pages(pages)
                .build();
    }
}
