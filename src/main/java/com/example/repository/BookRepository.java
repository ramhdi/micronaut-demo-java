package com.example.repository;

import com.example.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entities.
 * Follows JPA-like repository pattern conventions.
 */
public interface BookRepository {

    /**
     * Retrieve all books
     */
    List<Book> findAll();

    /**
     * Find a book by its ID
     */
    Optional<Book> findById(Long id);

    /**
     * Find books by title (case-insensitive partial match)
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find books by author (case-insensitive partial match)
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Find a book by exact ISBN match
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find books where stock quantity is less than the specified threshold
     */
    List<Book> findByStockQuantityLessThan(Integer threshold);

    /**
     * Save a new book or update an existing one
     * If the book has no ID, it will be created
     * If the book has an ID, it will be updated
     */
    Book save(Book book);

    /**
     * Save a book with a specific ID (for updates)
     */
    Book save(Long id, Book book);

    /**
     * Delete a book by its ID
     * Returns true if the book was deleted, false if it didn't exist
     */
    boolean deleteById(Long id);

    /**
     * Check if a book exists by ID
     */
    boolean existsById(Long id);

    /**
     * Check if a book exists by ISBN
     */
    boolean existsByIsbn(String isbn);

    /**
     * Get the total count of books
     */
    long count();

    /**
     * Delete all books (useful for testing)
     */
    void deleteAll();
}