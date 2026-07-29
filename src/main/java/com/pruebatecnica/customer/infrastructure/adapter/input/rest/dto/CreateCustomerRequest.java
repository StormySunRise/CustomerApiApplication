package com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank String name,
        @NotBlank @Email String email) {
}
