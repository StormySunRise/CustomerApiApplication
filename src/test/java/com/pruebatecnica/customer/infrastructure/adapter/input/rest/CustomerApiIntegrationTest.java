package com.pruebatecnica.customer.infrastructure.adapter.input.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pruebatecnica.customer.infrastructure.adapter.input.rest.dto.CreateCustomerRequest;
import com.pruebatecnica.customer.infrastructure.adapter.output.persistence.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CustomerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryCustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    private Long createCustomer(String name, String email) throws Exception {
        var result = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCustomerRequest(name, email))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void createCustomer_shouldReturn201WithGeneratedIdAndNormalizedEmail() throws Exception {
        var request = new CreateCustomerRequest("Juan Perez", "JUAN@EMAIL.COM");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan@email.com"))
                .andExpect(header().string("Location", startsWith("/customers/")));
    }

    @Test
    void getCreatedCustomer_shouldReturnSameData() throws Exception {
        Long id = createCustomer("Maria Lopez", "maria@email.com");

        mockMvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Maria Lopez"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void listCustomers_shouldReturnAllCustomersInDeterministicOrder() throws Exception {
        Long id1 = createCustomer("Ana", "ana@email.com");
        Long id2 = createCustomer("Zoe", "zoe@email.com");
        Long id3 = createCustomer("Bob", "bob@email.com");

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[1].id").value(id2))
                .andExpect(jsonPath("$[1].name").value("Zoe"))
                .andExpect(jsonPath("$[2].id").value(id3))
                .andExpect(jsonPath("$[2].name").value("Bob"));
    }

    @Test
    void createDuplicateEmail_shouldReturn409AndErrorStructure() throws Exception {
        createCustomer("First", "Customer@Example.com");

        var request = new CreateCustomerRequest("Second", "customer@example.com");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already exists: customer@example.com"))
                .andExpect(jsonPath("$.path").value("/customers"));

        mockMvc.perform(get("/customers"))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getNonExistingCustomer_shouldReturn404WithErrorStructure() throws Exception {
        mockMvc.perform(get("/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/customers/999"));
    }

    @Test
    void createWithEmptyName_shouldReturn400() throws Exception {
        var request = new CreateCustomerRequest("", "juan@email.com");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void createWithEmptyEmail_shouldReturn400() throws Exception {
        var request = new CreateCustomerRequest("Juan", "");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    void createWithInvalidEmail_shouldReturn400() throws Exception {
        var request = new CreateCustomerRequest("Juan", "invalido");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    void getByIdWithInvalidIdFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get("/customers/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid parameter: id"))
                .andExpect(jsonPath("$.path").value("/customers/abc"));
    }

    @Test
    void persistenceAcrossRequests_shouldKeepDataWithinTestContext() throws Exception {
        Long id1 = createCustomer("Persist1", "persist1@email.com");

        mockMvc.perform(get("/customers/{id}", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Persist1"));

        Long id2 = createCustomer("Persist2", "persist2@email.com");

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[1].id").value(id2));
    }

    @Test
    void createCustomer_shouldGenerateSequentialIds() throws Exception {
        Long id1 = createCustomer("First", "first@email.com");
        Long id2 = createCustomer("Second", "second@email.com");

        assertThat(id2, greaterThan(id1));
    }
}
