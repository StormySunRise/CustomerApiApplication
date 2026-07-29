package com.pruebatecnica.customer.application.service;

import com.pruebatecnica.customer.application.port.input.CreateCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.GetCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.ListCustomersUseCase;
import com.pruebatecnica.customer.application.port.output.CustomerRepositoryPort;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase, ListCustomersUseCase {

    private final CustomerRepositoryPort customerRepository;

    public CustomerService(CustomerRepositoryPort customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }
}
