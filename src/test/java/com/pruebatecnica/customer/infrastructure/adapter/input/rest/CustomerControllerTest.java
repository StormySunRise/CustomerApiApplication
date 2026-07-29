package com.pruebatecnica.customer.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pruebatecnica.customer.application.exception.CustomerNotFoundException;
import com.pruebatecnica.customer.application.port.input.CreateCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.GetCustomerUseCase;
import com.pruebatecnica.customer.application.port.input.ListCustomersUseCase;
import com.pruebatecnica.customer.domain.exception.DuplicateEmailException;
import com.pruebatecnica.customer.domain.model.Customer;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CreateCustomerRequest;
import com.pruebatecnica.customer.infrastructure.adapter.input.rest.mapper.CustomerMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
@Import(CustomerMapper.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private GetCustomerUseCase getCustomerUseCase;

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

    @Test
    void create_validRequest_returns201() throws Exception {
        var request = new CreateCustomerRequest("Juan Perez", "juan@email.com");
        var persisted = new Customer(1L, "Juan Perez", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenReturn(persisted);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_validRequest_returnsCustomerData() throws Exception {
        var request = new CreateCustomerRequest("Juan Perez", "juan@email.com");
        var persisted = new Customer(1L, "Juan Perez", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenReturn(persisted);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void create_validRequest_returnsLocationHeader() throws Exception {
        var request = new CreateCustomerRequest("Juan Perez", "juan@email.com");
        var persisted = new Customer(1L, "Juan Perez", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenReturn(persisted);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().string("Location", "/customers/1"));
    }

    @Test
    void create_emptyName_returns400WithValidationErrors() throws Exception {
        var request = new CreateCustomerRequest("", "juan@email.com");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void create_emptyEmail_returns400WithValidationErrors() throws Exception {
        var request = new CreateCustomerRequest("Juan", "");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    void create_invalidEmail_returns400WithValidationErrors() throws Exception {
        var request = new CreateCustomerRequest("Juan", "email-invalido");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    void create_invokesUseCaseWithCorrectData() throws Exception {
        var request = new CreateCustomerRequest("Juan", "juan@email.com");
        var persisted = new Customer(1L, "Juan", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenReturn(persisted);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        verify(createCustomerUseCase, times(1)).create(any());
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        var request = new CreateCustomerRequest("Juan", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenThrow(new DuplicateEmailException("juan@email.com"));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/customers"));
    }

    @Test
    void create_genericError_returns500() throws Exception {
        var request = new CreateCustomerRequest("Juan", "juan@email.com");
        when(createCustomerUseCase.create(any())).thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void getById_existingCustomer_returns200() throws Exception {
        var customer = new Customer(1L, "Juan", "juan@email.com");
        when(getCustomerUseCase.getById(1L)).thenReturn(customer);

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_existingCustomer_returnsCustomerData() throws Exception {
        var customer = new Customer(1L, "Juan", "juan@email.com");
        when(getCustomerUseCase.getById(1L)).thenReturn(customer);

        mockMvc.perform(get("/customers/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void getById_nonExisting_returns404() throws Exception {
        when(getCustomerUseCase.getById(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/customers/99"));
    }

    @Test
    void getById_invalidId_returns400WithErrorStructure() throws Exception {
        mockMvc.perform(get("/customers/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/customers/abc"));
    }

    @Test
    void getAll_returns200() throws Exception {
        when(listCustomersUseCase.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_returnsCustomers() throws Exception {
        var customers = List.of(
                new Customer(1L, "A", "a@email.com"),
                new Customer(2L, "B", "b@email.com"));
        when(listCustomersUseCase.getAll()).thenReturn(customers);

        mockMvc.perform(get("/customers"))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void getAll_empty_returnsEmptyList() throws Exception {
        when(listCustomersUseCase.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/customers"))
                .andExpect(jsonPath("$.size()").value(0));
    }
}
