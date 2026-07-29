package com.pruebatecnica.customer.domain.model;

import java.util.Locale;

public record Customer(Long id, String name, String email) {

    public Customer {
        name = requireNonBlank(name, "Name");
        email = requireNonBlank(email, "Email").toLowerCase(Locale.ROOT);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " must not be null or blank");
        return value.strip();
    }

    public Customer withId(Long id) {
        return new Customer(id, this.name, this.email);
    }
}
