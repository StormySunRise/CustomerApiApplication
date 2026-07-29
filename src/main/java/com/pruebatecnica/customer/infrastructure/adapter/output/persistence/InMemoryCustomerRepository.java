package com.pruebatecnica.customer.infrastructure.adapter.output.persistence;

import com.pruebatecnica.customer.application.port.output.CustomerRepositoryPort;
import com.pruebatecnica.customer.domain.exception.DuplicateEmailException;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCustomerRepository implements CustomerRepositoryPort {

    private final ConcurrentHashMap<Long, Customer> customersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public Customer save(Customer customer) {
        lock.lock();
        try {
            String email = customer.email();

            if (emailIndex.containsKey(email)) {
                throw new DuplicateEmailException(email);
            }

            long id = idGenerator.getAndIncrement();
            Customer persisted = customer.withId(id);

            customersById.put(id, persisted);
            emailIndex.put(email, id);

            return persisted;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(customersById.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return customersById.values().stream()
                .sorted(Comparator.comparingLong(Customer::id))
                .toList();
    }
}
