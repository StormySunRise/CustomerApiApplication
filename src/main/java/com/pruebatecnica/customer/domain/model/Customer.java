package com.pruebatecnica.customer.domain.model;

import java.util.Locale;

public record Customer(Long id, String name, String email) {

    public Customer {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }

        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        email = email.strip().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
    }

    public Customer withId(Long id) {
        return new Customer(id, this.name, this.email);
    }
}
