package com.pruebatecnica.customer.infrastructure.adapter.output.persistence;

import com.pruebatecnica.customer.application.port.output.CustomerRepositoryPort;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCustomerRepository implements CustomerRepositoryPort {

    private final ConcurrentHashMap<Long, Customer> customersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Customer save(Customer customer) {
        return null;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        return List.of();
    }
}
