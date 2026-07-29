package com.pruebatecnica.customer.application.port.input;

import com.pruebatecnica.customer.domain.model.Customer;

public interface GetCustomerUseCase {

    Customer getById(Long id);
}
