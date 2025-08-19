package com.example.service;

import com.example.model.Book;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Singleton
public class BookService {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public BookService() {
        // Initialize with some sample data
        initializeSampleData();
    }

    private void initializeSampleData() {
        create(new Book(
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "978-0-7432-7356-5",
                LocalDate.of(1925, 4, 10),
                new BigDecimal("15.99"),
                50,
                "A classic American novel about the Jazz Age"));

        create(new Book(
                "To Kill a Mockingbird",
                "Harper Lee",
                "978-0-06-112008-4",
                LocalDate.of(1960, 7, 11),
                new BigDecimal("14.99"),
                30,
                "A gripping tale of racial injustice and childhood innocence"));

        create(new Book(
                "1984",
                "George Orwell",
                "978-0-452-28423-4",
                LocalDate.of(1949, 6, 8),
                new BigDecimal("13.99"),
                25,
                "A dystopian social science fiction novel"));
    }

    /**
     * Get all books
     */
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    /**
     * Find a book by ID
     */
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    /**
     * Find books by title (case-insensitive partial match)
     */
    public List<Book> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchTitle = title.toLowerCase().trim();
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(searchTitle))
                .collect(Collectors.toList());
    }

    /**
     * Find books by author (case-insensitive partial match)
     */
    public List<Book> findByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchAuthor = author.toLowerCase().trim();
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(searchAuthor))
                .collect(Collectors.toList());
    }

    /**
     * Find a book by ISBN
     */
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }

        return books.values().stream()
                .filter(book -> book.getIsbn().equals(isbn.trim()))
                .findFirst();
    }

    /**
     * Create a new book
     */
    public Book create(Book book) {
        // Check if ISBN already exists
        if (findByIsbn(book.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("A book with ISBN " + book.getIsbn() + " already exists");
        }

        Long id = idGenerator.getAndIncrement();
        book.setId(id);
        books.put(id, book);
        return book;
    }

    /**
     * Update an existing book
     */
    public Optional<Book> update(Long id, Book updatedBook) {
        Book existingBook = books.get(id);
        if (existingBook == null) {
            return Optional.empty();
        }

        // Check if the new ISBN conflicts with another book
        if (!existingBook.getIsbn().equals(updatedBook.getIsbn())) {
            Optional<Book> bookWithSameIsbn = findByIsbn(updatedBook.getIsbn());
            if (bookWithSameIsbn.isPresent() && !bookWithSameIsbn.get().getId().equals(id)) {
                throw new IllegalArgumentException("A book with ISBN " + updatedBook.getIsbn() + " already exists");
            }
        }

        updatedBook.setId(id);
        books.put(id, updatedBook);
        return Optional.of(updatedBook);
    }

    /**
     * Delete a book by ID
     */
    public boolean delete(Long id) {
        return books.remove(id) != null;
    }

    /**
     * Get the total count of books
     */
    public long count() {
        return books.size();
    }

    /**
     * Check if a book exists by ID
     */
    public boolean exists(Long id) {
        return books.containsKey(id);
    }

    /**
     * Update stock quantity for a book
     */
    public Optional<Book> updateStock(Long id, Integer newQuantity) {
        Book book = books.get(id);
        if (book == null) {
            return Optional.empty();
        }

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        book.setStockQuantity(newQuantity);
        return Optional.of(book);
    }

    /**
     * Get books with low stock (less than specified threshold)
     */
    public List<Book> findBooksWithLowStock(int threshold) {
        return books.values().stream()
                .filter(book -> book.getStockQuantity() < threshold)
                .collect(Collectors.toList());
    }
}