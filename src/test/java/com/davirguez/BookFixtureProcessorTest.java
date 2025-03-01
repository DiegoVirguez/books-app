package com.davirguez;

import com.davirguez.model.BookFixture;
import com.davirguezc.com.BookProcessor;
import com.davirguezc.com.model.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BookFixtureProcessorTest {

    private static List<Book> books;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        books = objectMapper.readValue(new File("src/main/resources/books.json")
                , new TypeReference<>() {
                });
    }

    @Test
    void testFilterBooksByPageAndTitle() {
        List<Book> filteredBooks = BookProcessor.filterBooksByPageAndTitle(books);

        assertEquals(1, filteredBooks.size());
        assertEquals("Harry Potter and the Deathly Hallows", filteredBooks.get(0).getTitle());
    }

    @Test
    void testFilterBooksByAuthor() {
        List<Book> filteredBooks = BookProcessor.filterBooksByAuthor(books, "J.K. Rowling");

        assertEquals(2, filteredBooks.size());
        assertEquals("J.K. Rowling", filteredBooks.get(0).getAuthor().getName());
        assertEquals("J.K. Rowling", filteredBooks.get(1).getAuthor().getName());
        assertTrue(filteredBooks.stream().anyMatch(book -> book.getTitle().equals("Harry Potter and the Sorcerer's Stone")));
        assertTrue(filteredBooks.stream().anyMatch(book -> book.getTitle().equals("Harry Potter and the Deathly Hallows")));
    }

    @Test
    void testListBooksAndCountByAuthor() {
        Map<String, Long> result = BookProcessor.listBooksAndCountByAuthor(books);

        assertEquals(2, result.get("J.K. Rowling"));
        assertEquals(1, result.get("Kathryn"));
        assertEquals(1, result.get("Yann"));
        assertEquals(1, result.get("Khaled"));
        assertEquals(1, result.get("Suzanne"));
        assertEquals(1, result.get("Stephen"));

    }

    @Test
    void testConvertPublicationTimestamp() {
        List<Book> booksWithConvertedTimestamps = BookProcessor.convertPublicationTimestamp(books);

        assertNull(booksWithConvertedTimestamps.get(0).getPublicationTimestamp());
        assertEquals("1997-06-26", booksWithConvertedTimestamps.get(1).getPublicationTimestamp());
        assertEquals("2009-02-10", booksWithConvertedTimestamps.get(2).getPublicationTimestamp());
        assertNull( booksWithConvertedTimestamps.get(3).getPublicationTimestamp());
        assertEquals("2007-07-21", booksWithConvertedTimestamps.get(4).getPublicationTimestamp());
        assertEquals("2003-01-01", booksWithConvertedTimestamps.get(5).getPublicationTimestamp());
        assertEquals("1978-01-01", booksWithConvertedTimestamps.get(6).getPublicationTimestamp());
    }

    @Test
    void testCalculateAverageAndMinMaxPages() {
        Map<String, Object> result = BookProcessor.calculateAverageAndMinMaxPages(books);

        double averagePages = (double) result.get("averagePages");
        Book bookWithMostPages = (Book) result.get("bookWithMostPages");
        Book bookWithFewestPages = (Book) result.get("bookWithFewestPages");

        assertEquals(528.125, averagePages, 0.01);
        assertEquals("The Stand", bookWithMostPages.getTitle());
        assertEquals("Harry Potter and the Sorcerer's Stone", bookWithFewestPages.getTitle());
    }

    @Test
    void testAddWordCountAndGroupByAuthor() {
        Map<String, List<Book>> result = BookProcessor.addWordCountAndGroupByAuthor(books);

        for (Book book : books) {
            assertEquals(book.getPages() * 250L, book.getWordCount());
        }

        assertEquals(2, result.get("J.K. Rowling").size());
        assertEquals(1, result.get("Kathryn").size());
        assertEquals(1, result.get("Yann").size());
        assertEquals(1, result.get("Khaled").size());
        assertEquals(1, result.get("Suzanne").size());
        assertEquals(1, result.get("Stephen").size());
    }

    @Test
    void testCheckDuplicatesAndMissingPublicationTimestamp() {
        Map<String, List<Object>> result = BookProcessor.checkDuplicatesAndMissingPublicationTimestamp(books);

        List<Object> duplicateAuthors = result.get("duplicateAuthors");
        List<Object> booksWithoutTimestamp = result.get("booksWithoutTimestamp");

        assertNotNull(duplicateAuthors);
        assertNotNull(booksWithoutTimestamp);

        assertEquals(1, duplicateAuthors.size());
        assertEquals("J.K. Rowling", duplicateAuthors.get(0));

        assertEquals(2, booksWithoutTimestamp.size());
        assertTrue(booksWithoutTimestamp.stream().anyMatch(book -> ((Book) book).getTitle().equals("The Hunger Games")));
        assertTrue(booksWithoutTimestamp.stream().anyMatch(book -> ((Book) book).getTitle().equals("To Kill a Mockingbird")));
    }

    @Test
    void testFindMostRecentBooks() {
        List<Book> mostRecentBooks = BookProcessor.findMostRecentBooks(books);

        assertNotNull(mostRecentBooks);
        assertEquals(5, mostRecentBooks.size());
        assertEquals("The Help", mostRecentBooks.get(0).getTitle());
        assertEquals("Harry Potter and the Deathly Hallows", mostRecentBooks.get(1).getTitle());
        assertEquals("The Kite Runner", mostRecentBooks.get(2).getTitle());
        assertEquals("Life of Pi", mostRecentBooks.get(3).getTitle());
        assertEquals("Harry Potter and the Sorcerer's Stone", mostRecentBooks.get(4).getTitle());
    }

    @Test
    void testConvertJsonToCsv() throws IOException {
        List<Book> bookList = List.of(
                BookFixture.buildBook(1L,"Cien años de soledad", "Gabriel García Márquez", 432L),
                BookFixture.buildBook(2L,"Harry Potter y la piedra filosofal", "J.K. Rowling", 300L));

        BookProcessor.convertJsonToCsv(bookList);

        File jsonFile = new File("src/main/resources/myBook.json");
        File csvFile = new File("src/main/resources/myBook.csv");

        assertTrue(jsonFile.exists(), "JSON file should exist");
        assertTrue(csvFile.exists(), "CSV file should exist");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> booksFromJson = objectMapper.readValue(jsonFile, new TypeReference<>() {});
        assertEquals(bookList, booksFromJson, "Books in JSON file should match the original list");

        List<String> csvLines = Files.readAllLines(csvFile.toPath());
        assertEquals(3, csvLines.size(), "CSV file should have 3 lines (header + 2 books)");
        assertTrue(csvLines.get(1).contains("Cien años de soledad"), "CSV should contain the first book title");
        assertTrue(csvLines.get(2).contains("Harry Potter y la piedra filosofal"), "CSV should contain the second book title");
    }
}
