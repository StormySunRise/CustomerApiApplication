package com.pruebatecnica.customer.application.port.input;

import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;

public interface ListCustomersUseCase {

    List<Customer> getAll();
}
