package com.pruebatecnica.customer.infrastructure.adapter.input.rest;

import com.pruebatecnica.customer.application.port.input.CreateCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.GetCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.ListCustomersUseCase;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CreateCustomerRequest;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CustomerResponse;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.mapper.CustomerMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final CustomerMapper customerMapper;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase,
                              GetCustomerUseCase getCustomerUseCase,
                              ListCustomersUseCase listCustomersUseCase,
                              CustomerMapper customerMapper) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.customerMapper = customerMapper;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        var customer = createCustomerUseCase.create(customerMapper.toDomain(request));
        var response = customerMapper.toResponse(customer);
        return ResponseEntity.created(URI.create("/customers/" + customer.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        var customer = getCustomerUseCase.getById(id);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        var customers = listCustomersUseCase.getAll().stream()
                .map(customerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(customers);
    }
}
