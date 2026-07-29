package com.pruebatecnica.customer.application.port.input;

import com.pruebatecnica.customer.domain.model.Customer;
import java.util.Optional;

public interface GetCustomerUseCase {

    Optional<Customer> getById(Long id);
}
