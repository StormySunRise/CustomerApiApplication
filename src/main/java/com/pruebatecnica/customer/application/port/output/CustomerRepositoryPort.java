package com.pruebatecnica.customer.application.port.output;

import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();
}
