package com.example.repository;

import com.example.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookRepositoryTest {

    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = new MockBookRepository();
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Book book = new Book(
                "Test Book",
                "Test Author",
                "978-0-123456-78-9",
                LocalDate.of(2023, 1, 1),
                new BigDecimal("19.99"),
                10,
                "A test book");

        // When
        Book savedBook = bookRepository.save(book);

        // Then
        assertNotNull(savedBook.getId());
        assertEquals("Test Book", savedBook.getTitle());

        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        assertTrue(foundBook.isPresent());
        assertEquals(savedBook.getId(), foundBook.get().getId());
        assertEquals("Test Book", foundBook.get().getTitle());
    }

    @Test
    void testFindByIsbn() {
        // Given
        Book book = new Book(
                "Test Book",
                "Test Author",
                "978-0-123456-78-9",
                LocalDate.of(2023, 1, 1),
                new BigDecimal("19.99"),
                10,
                "A test book");
        bookRepository.save(book);

        // When
        Optional<Book> foundBook = bookRepository.findByIsbn("978-0-123456-78-9");

        // Then
        assertTrue(foundBook.isPresent());
        assertEquals("Test Book", foundBook.get().getTitle());
    }

    @Test
    void testFindByTitleContainingIgnoreCase() {
        // Given
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", "123",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Classic");
        Book book2 = new Book("Great Expectations", "Charles Dickens", "456",
                LocalDate.now(), new BigDecimal("12.99"), 5, "Classic");

        bookRepository.save(book1);
        bookRepository.save(book2);

        // When
        List<Book> foundBooks = bookRepository.findByTitleContainingIgnoreCase("great");

        // Then
        assertEquals(2, foundBooks.size());
        assertTrue(foundBooks.stream().anyMatch(book -> book.getTitle().contains("Great Gatsby")));
        assertTrue(foundBooks.stream().anyMatch(book -> book.getTitle().contains("Great Expectations")));
    }

    @Test
    void testFindByAuthorContainingIgnoreCase() {
        // Given
        Book book = new Book("Test Book", "Test Author", "123",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Test");
        bookRepository.save(book);

        // When
        List<Book> foundBooks = bookRepository.findByAuthorContainingIgnoreCase("test");

        // Then
        assertEquals(1, foundBooks.size());
        assertEquals("Test Author", foundBooks.get(0).getAuthor());
    }

    @Test
    void testFindByStockQuantityLessThan() {
        // Given
        Book book1 = new Book("Book 1", "Author 1", "123",
                LocalDate.now(), new BigDecimal("15.99"), 5, "Low stock");
        Book book2 = new Book("Book 2", "Author 2", "456",
                LocalDate.now(), new BigDecimal("12.99"), 15, "High stock");

        bookRepository.save(book1);
        bookRepository.save(book2);

        // When
        List<Book> lowStockBooks = bookRepository.findByStockQuantityLessThan(10);

        // Then
        assertEquals(1, lowStockBooks.size());
        assertEquals("Book 1", lowStockBooks.get(0).getTitle());
    }

    @Test
    void testDeleteById() {
        // Given
        Book book = new Book("Test Book", "Test Author", "123",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Test");
        Book savedBook = bookRepository.save(book);

        // When
        boolean deleted = bookRepository.deleteById(savedBook.getId());

        // Then
        assertTrue(deleted);
        assertFalse(bookRepository.existsById(savedBook.getId()));
    }

    @Test
    void testExistsByIsbn() {
        // Given
        Book book = new Book("Test Book", "Test Author", "978-0-123456-78-9",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Test");
        bookRepository.save(book);

        // When & Then
        assertTrue(bookRepository.existsByIsbn("978-0-123456-78-9"));
        assertFalse(bookRepository.existsByIsbn("978-0-999999-99-9"));
    }

    @Test
    void testCount() {
        // Given
        assertEquals(0, bookRepository.count());

        Book book1 = new Book("Book 1", "Author 1", "123",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Test");
        Book book2 = new Book("Book 2", "Author 2", "456",
                LocalDate.now(), new BigDecimal("12.99"), 10, "Test");

        // When
        bookRepository.save(book1);
        bookRepository.save(book2);

        // Then
        assertEquals(2, bookRepository.count());
    }

    @Test
    void testDeleteAll() {
        // Given
        Book book = new Book("Test Book", "Test Author", "123",
                LocalDate.now(), new BigDecimal("15.99"), 10, "Test");
        bookRepository.save(book);
        assertEquals(1, bookRepository.count());

        // When
        bookRepository.deleteAll();

        // Then
        assertEquals(0, bookRepository.count());
    }
}