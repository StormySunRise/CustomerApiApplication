package com.pruebatecnica.customer.application.service;

import com.pruebatecnica.customer.application.exception.CustomerNotFoundException;
import com.pruebatecnica.customer.application.port.input.CreateCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.GetCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.ListCustomersUseCase;
import com.pruebatecnica.customer.application.port.output.CustomerRepositoryPort;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;
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
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }
}
