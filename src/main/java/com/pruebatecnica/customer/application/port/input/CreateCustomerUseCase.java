package com.pruebatecnica.customer.application.port.input;

import com.pruebatecnica.customer.domain.model.Customer;

public interface CreateCustomerUseCase {

    Customer create(Customer customer);
}
