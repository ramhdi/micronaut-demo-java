package com.example;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(title = "Bookstore API", version = "1.0.0", description = "A simple CRUD API for managing books in a bookstore. "
        +
        "This API allows you to create, read, update, and delete books, " +
        "as well as search for books by various criteria.", license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT"), contact = @Contact(name = "Bookstore API Team", email = "api-support@bookstore.com", url = "https://bookstore.com/support")), servers = {
                @Server(url = "http://localhost:8000", description = "Development server")
        })
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}