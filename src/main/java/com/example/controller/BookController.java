package com.example.controller;

import com.example.model.Book;
import com.example.service.BookService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

@Controller("/api/books")
@Validated
@Tag(name = "Books", description = "Book management operations")
public class BookController {

    private final BookService bookService;

    @Inject
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Get
    @Operation(summary = "Get all books", description = "Retrieve a list of all books in the bookstore")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(schema = @Schema(implementation = Book[].class)))
    public HttpResponse<List<Book>> getAllBooks() {
        List<Book> books = bookService.findAll();
        return HttpResponse.ok(books);
    }

    @Get("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found", content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public HttpResponse<Book> getBookById(
            @Parameter(description = "Book ID", required = true, example = "1") @PathVariable @NotNull @Min(1) Long id) {

        Optional<Book> book = bookService.findById(id);
        return book.map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get("/search/title{?title}")
    @Operation(summary = "Search books by title", description = "Find books by title using partial matching (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Books matching the title search", content = @Content(schema = @Schema(implementation = Book[].class)))
    public HttpResponse<List<Book>> searchBooksByTitle(
            @Parameter(description = "Title to search for", example = "gatsby") @QueryValue(value = "title") @Nullable String title) {

        List<Book> books = bookService.findByTitle(title);
        return HttpResponse.ok(books);
    }

    @Get("/search/author{?author}")
    @Operation(summary = "Search books by author", description = "Find books by author using partial matching (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Books matching the author search", content = @Content(schema = @Schema(implementation = Book[].class)))
    public HttpResponse<List<Book>> searchBooksByAuthor(
            @Parameter(description = "Author to search for", example = "fitzgerald") @QueryValue(value = "author") @Nullable String author) {

        List<Book> books = bookService.findByAuthor(author);
        return HttpResponse.ok(books);
    }

    @Get("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieve a book by its ISBN number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found", content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public HttpResponse<Book> getBookByIsbn(
            @Parameter(description = "ISBN number", required = true, example = "978-0-7432-7356-5") @PathVariable String isbn) {

        Optional<Book> book = bookService.findByIsbn(isbn);
        return book.map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    @Operation(summary = "Create a new book", description = "Add a new book to the bookstore inventory")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully", content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "400", description = "Invalid book data or ISBN already exists")
    })
    public HttpResponse<Book> createBook(
            @Parameter(description = "Book to create", required = true) @Body @Valid Book book) {

        try {
            Book createdBook = bookService.create(book);
            return HttpResponse.created(createdBook);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Put("/{id}")
    @Operation(summary = "Update a book", description = "Update an existing book by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully", content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid book data or ISBN conflict")
    })
    public HttpResponse<Book> updateBook(
            @Parameter(description = "Book ID", required = true, example = "1") @PathVariable @NotNull @Min(1) Long id,
            @Parameter(description = "Updated book data", required = true) @Body @Valid Book book) {

        try {
            Optional<Book> updatedBook = bookService.update(id, book);
            return updatedBook.map(HttpResponse::ok)
                    .orElse(HttpResponse.notFound());
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Delete("/{id}")
    @Operation(summary = "Delete a book", description = "Remove a book from the bookstore inventory")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public HttpResponse<Void> deleteBook(
            @Parameter(description = "Book ID", required = true, example = "1") @PathVariable @NotNull @Min(1) Long id) {

        boolean deleted = bookService.delete(id);
        return deleted ? HttpResponse.noContent() : HttpResponse.notFound();
    }

    @Patch("/{id}/stock")
    @Operation(summary = "Update book stock", description = "Update the stock quantity for a specific book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated successfully", content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid stock quantity")
    })
    public HttpResponse<Book> updateBookStock(
            @Parameter(description = "Book ID", required = true, example = "1") @PathVariable @NotNull @Min(1) Long id,
            @Parameter(description = "New stock quantity", required = true, example = "25") @QueryValue @NotNull @Min(0) Integer quantity) {

        try {
            Optional<Book> updatedBook = bookService.updateStock(id, quantity);
            return updatedBook.map(HttpResponse::ok)
                    .orElse(HttpResponse.notFound());
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest();
        }
    }

    @Get("/count")
    @Operation(summary = "Get total book count", description = "Get the total number of books in the inventory")
    @ApiResponse(responseCode = "200", description = "Book count retrieved successfully", content = @Content(schema = @Schema(implementation = Long.class)))
    public HttpResponse<Long> getBookCount() {
        long count = bookService.count();
        return HttpResponse.ok(count);
    }

    @Get("/low-stock{?threshold}")
    @Operation(summary = "Get books with low stock", description = "Find books with stock quantity below the specified threshold")
    @ApiResponse(responseCode = "200", description = "Books with low stock retrieved successfully", content = @Content(schema = @Schema(implementation = Book[].class)))
    public HttpResponse<List<Book>> getBooksWithLowStock(
            @Parameter(description = "Stock threshold", example = "10") @QueryValue(value = "threshold", defaultValue = "10") @Min(0) Integer threshold) {

        List<Book> books = bookService.findBooksWithLowStock(threshold);
        return HttpResponse.ok(books);
    }
}