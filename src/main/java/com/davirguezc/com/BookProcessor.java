package com.davirguezc.com;

import com.davirguezc.com.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BookProcessor {

    static Logger logger = LoggerFactory.getLogger(BookProcessor.class);

    public static void main(String[] args) {
    }

    /*
    * Filtra los libros con más de 400 páginas y aquellos cuyo título contenga "Harry".
    * */
    public static List<Book> filterBooksByPageAndTitle(List<Book> books) {
        return books.stream()
                .filter(book -> book.getPages() > 400)
                .filter(book -> book.getTitle().contains("Harry"))
                .toList();
    }

    /*
    * Obtén los libros escritos por "J.K. Rowling".
    * */
    public static List<Book> filterBooksByAuthor(List<Book> books, String authorName) {
        return books.stream()
                .filter(book -> book.getAuthor().getName().equals(authorName))
                .toList();
    }

    /*
    * Lista los títulos ordenados alfabéticamente y cuenta cuántos libros ha escrito cada autor.
    * */
    public static Map<String, Long> listBooksAndCountByAuthor(List<Book> books) {

        List<String> sortedTitles = books.stream()
                .sorted(Comparator.comparing(Book::getTitle))
                .map(Book::getTitle)
                .toList();

        logger.info("--Books sorted alphabetically:");
        sortedTitles.forEach(title -> logger.info(title));

        Map<String, Long> booksCountByAuthor = books.stream()
                .collect(Collectors.groupingBy(book -> book.getAuthor().getName(), Collectors.counting()));

        logger.info("--Number of books by author:");
        booksCountByAuthor.forEach((author, count) ->
                logger.info("{}: {}", author, count));

        return booksCountByAuthor;
    }

    /*
    * Convierte publicationTimestamp a formato AAAA-MM-DD
    * */
    public static List<Book> convertPublicationTimestamp(List<Book> books) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return books.stream()
                .map(book -> {
                    if (Objects.nonNull(book.getPublicationTimestamp())) {
                        long unixTimestamp = Long.parseLong(book.getPublicationTimestamp());
                        LocalDate date = Instant.ofEpochSecond(unixTimestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        book.setPublicationTimestamp(date.format(formatter));
                    }
                    return book;
                })
                .toList();
    }

    /*
    * Calcula el promedio de páginas y encuentra el libro con más y menos páginas.
    * */
    public static Map<String, Object> calculateAverageAndMinMaxPages(List<Book> books) {
        double averagePages = books.stream()
                .mapToLong(Book::getPages)
                .average()
                .orElse(0);

        Book bookWithMostPages = books.stream()
                .max(Comparator.comparing(Book::getPages))
                .orElse(null);

        Book bookWithFewestPages = books.stream()
                .min(Comparator.comparing(Book::getPages))
                .orElse(null);

        logger.info("Average number of pages: {}", averagePages);
        logger.info("Book with the most pages: {}", bookWithMostPages);
        logger.info("Book with the fewest pages: {}", bookWithFewestPages);

        return  Map.of(
                "averagePages", averagePages,
                "bookWithMostPages", bookWithMostPages,
                "bookWithFewestPages", bookWithFewestPages
        );

    }

    /*
    * Añade un campo wordCount (250 palabras por página) y agrupa los libros por autor.
    * */
    public static Map<String, List<Book>> addWordCountAndGroupByAuthor(List<Book> books) {
        return books.stream()
                .map(book -> {
                    book.setWordCount(book.getPages() * 250L);
                    return book;
                })
                .collect(Collectors.groupingBy(book -> book.getAuthor().getName()));
    }

    /*
    * Verifica si hay autores duplicados y encuentra los libros sin publicationTimestamp
    * */
    public static Map<String, List<Object>> checkDuplicatesAndMissingPublicationTimestamp(List<Book> books) {

        List<String> duplicateAuthors = books.stream()
                .collect(Collectors.groupingBy(book -> book.getAuthor().getName(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        List<Book> booksWithoutTimestamp = books.stream()
                .filter(book -> Objects.isNull(book.getPublicationTimestamp()))
                .toList();

        return Map.of(
                "duplicateAuthors", new ArrayList<>(duplicateAuthors),
                "booksWithoutTimestamp", new ArrayList<>(booksWithoutTimestamp)
        );
    }

    /*
    * Identifica los libros más recientes.
    * */
    public static List<Book> findMostRecentBooks(List<Book> books) {
        return books.stream()
                .filter(book -> Objects.nonNull(book.getPublicationTimestamp()))
                .sorted(Comparator.comparing(book -> Instant.ofEpochSecond(Long.parseLong(book.getPublicationTimestamp())), Comparator.reverseOrder()))
                .limit(5)
                .toList();
    }

    /*
    * Genera un JSON con títulos y autores y exporta la lista a CSV
    * */
    public static void convertJsonToCsv(List<Book> books) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(books);
        logger.info("Generated JSON:\n{}", json);

        objectMapper.writeValue(new File("src/main/resources/myBook.json"), books);

        exportToCSV(books);
        logger.info("CSV generated successfully.");
    }

    private static void exportToCSV(List<Book> books) throws IOException {
        try (FileWriter writer = new FileWriter("src/main/resources/myBook.csv");
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create()
                     .setHeader("Id", "Title", "Author", "Pages")
                     .build())) {

            for (Book book : books) {
                csvPrinter.printRecord(book.getId(), book.getTitle(),
                        book.getAuthor().getName(), book.getPages());
            }
        }
    }
}