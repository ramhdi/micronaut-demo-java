package com.example.repository;

import com.example.model.Book;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of BookRepository.
 * This simulates a database using ConcurrentHashMap for thread-safety.
 * In a real application, this would be replaced with JPA/Micronaut Data
 * implementation.
 */
@Singleton
public class MockBookRepository implements BookRepository {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public Optional<Book> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public List<Book> findByTitleContainingIgnoreCase(String title) {
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchTitle = title.toLowerCase().trim();
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(searchTitle))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByAuthorContainingIgnoreCase(String author) {
        if (author == null || author.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchAuthor = author.toLowerCase().trim();
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(searchAuthor))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }

        return books.values().stream()
                .filter(book -> book.getIsbn().equals(isbn.trim()))
                .findFirst();
    }

    @Override
    public List<Book> findByStockQuantityLessThan(Integer threshold) {
        if (threshold == null) {
            return Collections.emptyList();
        }

        return books.values().stream()
                .filter(book -> book.getStockQuantity() < threshold)
                .collect(Collectors.toList());
    }

    @Override
    public Book save(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        // If book has no ID, treat as new entity
        if (book.getId() == null) {
            Long id = idGenerator.getAndIncrement();
            book.setId(id);
        }

        // Create a copy to avoid external modifications
        Book bookCopy = createBookCopy(book);
        books.put(bookCopy.getId(), bookCopy);
        return bookCopy;
    }

    @Override
    public Book save(Long id, Book book) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        book.setId(id);
        Book bookCopy = createBookCopy(book);
        books.put(id, bookCopy);
        return bookCopy;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return books.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return books.containsKey(id);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        return books.values().stream()
                .anyMatch(book -> book.getIsbn().equals(isbn.trim()));
    }

    @Override
    public long count() {
        return books.size();
    }

    @Override
    public void deleteAll() {
        books.clear();
        idGenerator.set(1);
    }

    /**
     * Creates a defensive copy of a book to prevent external modifications
     */
    private Book createBookCopy(Book original) {
        return new Book(
                original.getId(),
                original.getTitle(),
                original.getAuthor(),
                original.getIsbn(),
                original.getPublicationDate(),
                original.getPrice(),
                original.getStockQuantity(),
                original.getDescription());
    }
}