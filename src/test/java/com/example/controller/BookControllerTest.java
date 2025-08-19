package com.example.controller;

import com.example.model.Book;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

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

    @Test
    void testGetAllBooks() {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        // Should have the 3 sample books initially
        assertEquals(3, books.size());
    }

    @Test
    void testGetBookById() {
        BlockingHttpClient client = httpClient.toBlocking();

        // Test getting an existing book
        HttpResponse<Book> response = client.exchange(
                HttpRequest.GET("/api/books/1"),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book book = response.body();
        assertNotNull(book);
        assertEquals(1L, book.getId());
        assertEquals("The Great Gatsby", book.getTitle());
    }

    @Test
    void testGetBookById_NotFound() {
        BlockingHttpClient client = httpClient.toBlocking();

        HttpResponse<Book> response = client.exchange(
                HttpRequest.GET("/api/books/999"),
                Book.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void testCreateBook() {
        BlockingHttpClient client = httpClient.toBlocking();

        Book newBook = new Book(
                "Test Book",
                "Test Author",
                "978-0-123456-78-9",
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
        assertEquals("Test Book", createdBook.getTitle());
        assertEquals("Test Author", createdBook.getAuthor());
    }

    @Test
    void testUpdateBook() {
        BlockingHttpClient client = httpClient.toBlocking();

        // First, get an existing book
        Book existingBook = client.retrieve(
                HttpRequest.GET("/api/books/1"),
                Book.class);

        // Update the book
        existingBook.setTitle("Updated Title");
        existingBook.setPrice(new BigDecimal("25.99"));

        HttpResponse<Book> response = client.exchange(
                HttpRequest.PUT("/api/books/1", existingBook),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book updatedBook = response.body();
        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals(new BigDecimal("25.99"), updatedBook.getPrice());
    }

    @Test
    void testSearchBooksByTitle() {
        BlockingHttpClient client = httpClient.toBlocking();

        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books/search/title?title=gatsby"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.get(0).getTitle().toLowerCase().contains("gatsby"));
    }

    @Test
    void testSearchBooksByAuthor() {
        BlockingHttpClient client = httpClient.toBlocking();

        HttpResponse<List<Book>> response = client.exchange(
                HttpRequest.GET("/api/books/search/author?author=orwell"),
                Argument.listOf(Book.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        List<Book> books = response.body();
        assertNotNull(books);
        assertFalse(books.isEmpty());
        assertTrue(books.get(0).getAuthor().toLowerCase().contains("orwell"));
    }

    @Test
    void testGetBookCount() {
        BlockingHttpClient client = httpClient.toBlocking();

        HttpResponse<Long> response = client.exchange(
                HttpRequest.GET("/api/books/count"),
                Long.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Long count = response.body();
        assertNotNull(count);
        assertTrue(count >= 3); // At least the 3 sample books
    }

    @Test
    void testUpdateBookStock() {
        BlockingHttpClient client = httpClient.toBlocking();

        HttpResponse<Book> response = client.exchange(
                HttpRequest.PATCH("/api/books/1/stock?quantity=100", null),
                Book.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        Book updatedBook = response.body();
        assertNotNull(updatedBook);
        assertEquals(100, updatedBook.getStockQuantity());
    }

    @Test
    void testDeleteBook() {
        BlockingHttpClient client = httpClient.toBlocking();

        // First create a book to delete
        Book newBook = new Book(
                "Book to Delete",
                "Delete Author",
                "978-0-999999-99-9",
                LocalDate.of(2023, 1, 1),
                new BigDecimal("9.99"),
                5,
                "A book to be deleted");

        Book createdBook = client.retrieve(
                HttpRequest.POST("/api/books", newBook),
                Book.class);

        // Now delete it
        HttpResponse<Void> deleteResponse = client.exchange(
                HttpRequest.DELETE("/api/books/" + createdBook.getId()),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());

        // Verify it's deleted
        HttpResponse<Book> getResponse = client.exchange(
                HttpRequest.GET("/api/books/" + createdBook.getId()),
                Book.class);

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatus());
    }
}