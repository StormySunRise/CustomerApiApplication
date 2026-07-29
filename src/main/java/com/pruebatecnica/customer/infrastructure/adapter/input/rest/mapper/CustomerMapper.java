package com.pruebatecnica.customer.infrastructure.adapter.input.rest.mapper;

import com.pruebatecnica.customer.domain.model.Customer;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CreateCustomerRequest;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDomain(CreateCustomerRequest request) {
        return new Customer(null, request.name(), request.email());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.id(), customer.name(), customer.email());
    }
}
