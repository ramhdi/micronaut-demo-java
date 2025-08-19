package com.example.config;

import com.example.repository.BookRepository;
import com.example.repository.MockBookRepository;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

/**
 * Configuration class for repository beans.
 * This allows for easy switching between different repository implementations.
 */
@Factory
public class RepositoryConfiguration {

    /**
     * Creates the primary BookRepository implementation.
     * Currently using in-memory implementation, but this can be easily
     * switched to JPA, Micronaut Data, or any other persistence technology.
     */
    @Singleton
    @Primary
    public BookRepository bookRepository() {
        return new MockBookRepository();
    }

    /*
     * Example of how you might configure different repository implementations:
     * 
     * @Singleton
     * 
     * @Named("jpa")
     * public BookRepository jpaBookRepository() {
     * return new JpaBookRepository();
     * }
     * 
     * @Singleton
     * 
     * @Named("mongo")
     * public BookRepository mongoBookRepository() {
     * return new MongoBookRepository();
     * }
     * 
     * @Singleton
     * 
     * @Named("redis")
     * public BookRepository redisBookRepository() {
     * return new RedisBookRepository();
     * }
     */
}