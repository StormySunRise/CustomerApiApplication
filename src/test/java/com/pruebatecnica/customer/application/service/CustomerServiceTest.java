package com.pruebatecnica.customer.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pruebatecnica.customer.application.exception.CustomerNotFoundException;
import com.pruebatecnica.customer.application.port.output.CustomerRepositoryPort;
import com.pruebatecnica.customer.domain.exception.DuplicateEmailException;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void create_success() {
        var customer = new Customer(null, "Juan", "juan@email.com");
        var persisted = new Customer(1L, "Juan", "juan@email.com");
        when(customerRepository.save(customer)).thenReturn(persisted);

        var result = customerService.create(customer);

        assertNotNull(result.id());
        assertEquals(1L, result.id());
        assertEquals("Juan", result.name());
    }

    @Test
    void create_delegatesToRepository() {
        var customer = new Customer(null, "Juan", "juan@email.com");
        customerService.create(customer);

        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void create_returnsPersistedCustomer() {
        var customer = new Customer(null, "Juan", "juan@email.com");
        var persisted = new Customer(1L, "Juan", "juan@email.com");
        when(customerRepository.save(customer)).thenReturn(persisted);

        var result = customerService.create(customer);

        assertSame(persisted, result);
    }

    @Test
    void create_propagatesDuplicateEmailException() {
        var customer = new Customer(null, "Juan", "juan@email.com");
        when(customerRepository.save(customer)).thenThrow(new DuplicateEmailException("juan@email.com"));

        assertThrows(DuplicateEmailException.class, () -> customerService.create(customer));
    }

    @Test
    void create_doesNotCheckEmailExistenceBeforehand() {
        var customer = new Customer(null, "Juan", "juan@email.com");
        customerService.create(customer);

        verify(customerRepository, never()).findById(any());
        verify(customerRepository, never()).findAll();
    }

    @Test
    void getById_existingCustomer_returnsCustomer() {
        var customer = new Customer(1L, "Juan", "juan@email.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        var result = customerService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Juan", result.name());
    }

    @Test
    void getById_nonExisting_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getById(99L));
    }

    @Test
    void getById_queriesRepositoryWithCorrectId() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer(1L, "Juan", "juan@email.com")));

        customerService.getById(1L);

        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getAll_returnsAllCustomers() {
        var customers = List.of(
                new Customer(1L, "A", "a@email.com"),
                new Customer(2L, "B", "b@email.com"));
        when(customerRepository.findAll()).thenReturn(customers);

        var result = customerService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getAll_empty_returnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        var result = customerService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_delegatesToRepository() {
        customerService.getAll();

        verify(customerRepository, times(1)).findAll();
    }
}
