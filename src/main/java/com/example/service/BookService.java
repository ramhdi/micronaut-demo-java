package com.example.service;

import com.example.model.Book;
import com.example.repository.BookRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Book operations.
 * Contains business logic and delegates data access to BookRepository.
 */
@Singleton
public class BookService {

    private final BookRepository bookRepository;

    @Inject
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        // Initialize with sample data only if no books exist
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (bookRepository.count() == 0) {
            // Initialize with some sample data
            createSampleBooks();
        }
    }

    private void createSampleBooks() {
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
        return bookRepository.findAll();
    }

    /**
     * Find a book by ID
     */
    public Optional<Book> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return bookRepository.findById(id);
    }

    /**
     * Find books by title (case-insensitive partial match)
     */
    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Find books by author (case-insensitive partial match)
     */
    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    /**
     * Find a book by ISBN
     */
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        return bookRepository.findByIsbn(isbn.trim());
    }

    /**
     * Create a new book
     */
    public Book create(Book book) {
        validateBookForCreation(book);

        // Business rule: Check if ISBN already exists
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("A book with ISBN " + book.getIsbn() + " already exists");
        }

        // Ensure ID is null for new entities
        book.setId(null);

        return bookRepository.save(book);
    }

    /**
     * Update an existing book
     */
    public Optional<Book> update(Long id, Book updatedBook) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        validateBookForUpdate(updatedBook);

        // Check if book exists
        if (!bookRepository.existsById(id)) {
            return Optional.empty();
        }

        // Business rule: Check if the new ISBN conflicts with another book
        Optional<Book> existingBookWithSameIsbn = bookRepository.findByIsbn(updatedBook.getIsbn());
        if (existingBookWithSameIsbn.isPresent() && !existingBookWithSameIsbn.get().getId().equals(id)) {
            throw new IllegalArgumentException("A book with ISBN " + updatedBook.getIsbn() + " already exists");
        }

        Book savedBook = bookRepository.save(id, updatedBook);
        return Optional.of(savedBook);
    }

    /**
     * Delete a book by ID
     */
    public boolean delete(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return bookRepository.deleteById(id);
    }

    /**
     * Get the total count of books
     */
    public long count() {
        return bookRepository.count();
    }

    /**
     * Check if a book exists by ID
     */
    public boolean exists(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return bookRepository.existsById(id);
    }

    /**
     * Update stock quantity for a book
     */
    public Optional<Book> updateStock(Long id, Integer newQuantity) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be null or negative");
        }

        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isEmpty()) {
            return Optional.empty();
        }

        Book book = bookOpt.get();
        book.setStockQuantity(newQuantity);

        Book updatedBook = bookRepository.save(id, book);
        return Optional.of(updatedBook);
    }

    /**
     * Get books with low stock (less than specified threshold)
     */
    public List<Book> findBooksWithLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        return bookRepository.findByStockQuantityLessThan(threshold);
    }

    /**
     * Validate book data for creation
     */
    private void validateBookForCreation(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        validateCommonBookFields(book);
    }

    /**
     * Validate book data for update
     */
    private void validateBookForUpdate(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        validateCommonBookFields(book);
    }

    /**
     * Common validation for book fields
     */
    private void validateCommonBookFields(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Book author is required");
        }

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN is required");
        }

        if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Book price must be greater than 0");
        }

        if (book.getStockQuantity() == null || book.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        if (book.getPublicationDate() == null) {
            throw new IllegalArgumentException("Publication date is required");
        }

        if (book.getPublicationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Publication date cannot be in the future");
        }
    }
}