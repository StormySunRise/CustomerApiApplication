package com.pruebatecnica.customer.domain.model;

public record Customer(Long id, String name, String email) {

    public Customer withId(Long id) {
        return new Customer(id, this.name, this.email);
    }
}
