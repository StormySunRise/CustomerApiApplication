package com.pruebatecnica.customer.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void createValidCustomer() {
        var customer = new Customer(null, "Juan Perez", "juan@email.com");
        assertNull(customer.id());
        assertEquals("Juan Perez", customer.name());
        assertEquals("juan@email.com", customer.email());
    }

    @Test
    void normalizeName() {
        var customer = new Customer(null, "  Juan Perez  ", "juan@email.com");
        assertEquals("Juan Perez", customer.name());
    }

    @Test
    void rejectNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, null, "juan@email.com"));
    }

    @Test
    void rejectEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "", "juan@email.com"));
    }

    @Test
    void rejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "   ", "juan@email.com"));
    }

    @Test
    void normalizeEmailToLowercase() {
        var customer = new Customer(null, "Juan", "JUAN@EMAIL.COM");
        assertEquals("juan@email.com", customer.email());
    }

    @Test
    void normalizeEmailTrim() {
        var customer = new Customer(null, "Juan", "  JUAN@EMAIL.COM  ");
        assertEquals("juan@email.com", customer.email());
    }

    @Test
    void rejectNullEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Juan", null));
    }

    @Test
    void rejectEmptyEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Juan", ""));
    }

    @Test
    void createCopyWithId() {
        var original = new Customer(null, "Juan", "juan@email.com");
        var withId = original.withId(1L);
        assertEquals(1L, withId.id());
        assertEquals(original.name(), withId.name());
        assertEquals(original.email(), withId.email());
    }

    @Test
    void immutability() {
        var original = new Customer(null, "Juan", "juan@email.com");
        var withId = original.withId(1L);
        assertNull(original.id());
        assertEquals(1L, withId.id());
        assertNotSame(original, withId);
    }
}
