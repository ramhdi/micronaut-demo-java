package com.example.controller;

import com.example.model.Book;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class BookControllerTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    private BlockingHttpClient client;

    @BeforeEach
    void setUp() {
        client = httpClient.toBlocking();
        // Ensure we have some initial data by checking count and creating if needed
        ensureInitialData();
    }

    private void ensureInitialData() {
        try {
            HttpResponse<Long> countResponse = client.exchange(
                    HttpRequest.GET("/api/books/count"),
                    Long.class);

            Long count = countResponse.body();
            if (count == 0) {
                // Create some test data
                createTestBook("The Great Gatsby", "F. Scott Fitzgerald", "978-0-7432-7356-5");
                createTestBook("To Kill a Mockingbird", "Harper Lee", "978-0-06-112008-4");
                createTestBook("1984", "George Orwell", "978-0-452-28423-4");
            }
        } catch (Exception e) {
            // If count fails, try to create test data anyway
            try {
                createTestBook("The Great Gatsby", "F. Scott Fitzgerald", "978-0-7432-7356-5");
            } catch (Exception ignored) {
                // Ignore if already exists
            }
        }
    }

    private Book createTestBook(String title, String author, String isbn) {
        Book book = new Book(
                title,
                author,
                isbn,
                LocalDate.of(2020, 1, 1),
                new BigDecimal("15.99"),
                50,
                "Test description");

        try {
            return client.retrieve(HttpRequest.POST("/api/books", book), Book.class);
        } catch (HttpClientResponseException e) {
            if (e.getStatus() == HttpStatus.BAD_REQUEST) {
                // Book might already exist, try to find it
                try {
                    return client.retrieve(HttpRequest.GET("/api/books/isbn/" + isbn), Book.class);
                } catch (Exception ignored) {
                    throw e;
                }
            }
            throw e;
        }
    }

    @Test
    void testGetAllBooks() {
        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        // Should have at least the test books
        assertTrue(books.size() >= 3);
    }

    @Test
    void testGetBookById() {
        // First, get all books to find a valid ID
        List<Book> allBooks = client.retrieve(
                HttpRequest.GET("/api/books"),
                Argument.listOf(Book.class));

        assertFalse(allBooks.isEmpty(), "No books found in the system");

        Book firstBook = allBooks.get(0);
        Long validId = firstBook.getId();

        // Test getting an existing book
        HttpResponse<Book> response = client.exchange(
                HttpRequest.GET("/api/books/" + validId),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book book = response.body();
        assertNotNull(book);
        assertEquals(validId, book.getId());
        assertNotNull(book.getTitle());
    }

    @Test
    void testGetBookById_NotFound() {
        // Test with a very high ID that shouldn't exist
        long nonExistentId = 999999L;

        try {
            client.exchange(
                    HttpRequest.GET("/api/books/" + nonExistentId),
                    Book.class);
            fail("Expected HttpClientResponseException for non-existent book");
        } catch (HttpClientResponseException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    void testCreateBook() {
        Book newBook = new Book(
                "Test Book " + System.currentTimeMillis(), // Make title unique
                "Test Author",
                "978-0-123456-78-" + (System.currentTimeMillis() % 10), // Make ISBN unique
                LocalDate.of(2023, 1, 1),
                new BigDecimal("19.99"),
                10,
                "A test book");

        HttpResponse<Book> response = client.exchange(
                HttpRequest.POST("/api/books", newBook),
                Book.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        Book createdBook = response.body();
        assertNotNull(createdBook);
        assertNotNull(createdBook.getId());
        assertEquals(newBook.getTitle(), createdBook.getTitle());
        assertEquals(newBook.getAuthor(), createdBook.getAuthor());
    }

    @Test
    void testUpdateBook() {
        // First create a book to update
        Book newBook = new Book(
                "Book to Update " + System.currentTimeMillis(),
                "Update Author",
                "978-0-UPDATE-00-" + (System.currentTimeMillis() % 10),
                LocalDate.of(2023, 1, 1),
                new BigDecimal("19.99"),
                10,
                "A book to update");

        Book createdBook = client.retrieve(
                HttpRequest.POST("/api/books", newBook),
                Book.class);

        // Update the book
        createdBook.setTitle("Updated Title");
        createdBook.setPrice(new BigDecimal("25.99"));

        HttpResponse<Book> response = client.exchange(
                HttpRequest.PUT("/api/books/" + createdBook.getId(), createdBook),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book updatedBook = response.body();
        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals(new BigDecimal("25.99"), updatedBook.getPrice());
    }

    @Test
    void testSearchBooksByTitle() {
        // Ensure we have a book with "Gatsby" in the title
        ensureGatsbyBookExists();

        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books/search/title?title=gatsby"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.get(0).getTitle().toLowerCase().contains("gatsby"));
    }

    private void ensureGatsbyBookExists() {
        try {
            // Try to find Gatsby book first
            HttpResponse<List<Book>> response = client.exchange(
                    HttpRequest.GET("/api/books/search/title?title=gatsby"),
                    Argument.listOf(Book.class));

            List<Book> books = response.body();
            if (books == null || books.isEmpty()) {
                // Create it if it doesn't exist
                createTestBook("The Great Gatsby", "F. Scott Fitzgerald",
                        "978-0-7432-7356-" + System.currentTimeMillis() % 10);
            }
        } catch (Exception e) {
            // Create it if search fails
            createTestBook("The Great Gatsby", "F. Scott Fitzgerald",
                    "978-0-7432-7356-" + System.currentTimeMillis() % 10);
        }
    }

    @Test
    void testSearchBooksByAuthor() {
        // Ensure we have a book by Orwell
        ensureOrwellBookExists();

        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books/search/author?author=orwell"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.get(0).getAuthor().toLowerCase().contains("orwell"));
    }

    private void ensureOrwellBookExists() {
        try {
            // Try to find Orwell book first
            HttpResponse<List<Book>> response = client.exchange(
                    HttpRequest.GET("/api/books/search/author?author=orwell"),
                    Argument.listOf(Book.class));

            List<Book> books = response.body();
            if (books == null || books.isEmpty()) {
                // Create it if it doesn't exist
                createTestBook("1984", "George Orwell",
                        "978-0-452-28423-" + System.currentTimeMillis() % 10);
            }
        } catch (Exception e) {
            // Create it if search fails
            createTestBook("1984", "George Orwell", "978-0-452-28423-" + System.currentTimeMillis() % 10);
        }
    }

    @Test
    void testGetBookCount() {
        HttpResponse<Long> response = client.exchange(
                HttpRequest.GET("/api/books/count"),
                Long.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Long count = response.body();
        assertNotNull(count);
        assertTrue(count >= 0); // Should have at least 0 books
    }

    @Test
    void testUpdateBookStock() {
        // First create a book to update stock for
        Book newBook = new Book(
                "Stock Test Book " + System.currentTimeMillis(),
                "Stock Author",
                "978-0-STOCK-00-" + (System.currentTimeMillis() % 10),
                LocalDate.of(2023, 1, 1),
                new BigDecimal("19.99"),
                50,
                "A book for stock testing");

        Book createdBook = client.retrieve(
                HttpRequest.POST("/api/books", newBook),
                Book.class);

        HttpResponse<Book> response = client.exchange(
                HttpRequest.PATCH("/api/books/" + createdBook.getId() + "/stock?quantity=100", null),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book updatedBook = response.body();
        assertNotNull(updatedBook);
        assertEquals(100, updatedBook.getStockQuantity());
    }

    @Test
    void testDeleteBook() {
        // First create a book to delete
        Book newBook = new Book(
                "Book to Delete " + System.currentTimeMillis(),
                "Delete Author",
                "978-0-DELETE-0-" + (System.currentTimeMillis() % 10),
                LocalDate.of(2023, 1, 1),
                new BigDecimal("9.99"),
                5,
                "A book to be deleted");

        Book createdBook = client.retrieve(
                HttpRequest.POST("/api/books", newBook),
                Book.class);

        // Now delete it
        try {
            HttpResponse<Void> deleteResponse = client.exchange(
                    HttpRequest.DELETE("/api/books/" + createdBook.getId()),
                    Void.class);
            assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
        } catch (HttpClientResponseException e) {
            // If we get an exception, check if it's the expected NO_CONTENT status
            if (e.getStatus() != HttpStatus.NO_CONTENT) {
                fail("Unexpected status code: " + e.getStatus());
            }
        }

        // Verify it's deleted
        try {
            client.exchange(
                    HttpRequest.GET("/api/books/" + createdBook.getId()),
                    Book.class);
            fail("Expected HttpClientResponseException for deleted book");
        } catch (HttpClientResponseException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    void testGetBooksWithLowStock() {
        // Create a book with low stock
        Book lowStockBook = new Book(
                "Low Stock Book " + System.currentTimeMillis(),
                "Low Stock Author",
                "978-0-LOWSTK-0-" + (System.currentTimeMillis() % 10),
                LocalDate.of(2023, 1, 1),
                new BigDecimal("9.99"),
                3, // Low stock
                "A book with low stock");

        client.retrieve(HttpRequest.POST("/api/books", lowStockBook), Book.class);

        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books/low-stock?threshold=10"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        // Should contain at least our low stock book
        assertTrue(books.stream().anyMatch(book -> book.getStockQuantity() < 10));
    }
}