package com.pruebatecnica.customer.infrastructure.adapter.output.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.pruebatecnica.customer.domain.exception.DuplicateEmailException;
import com.pruebatecnica.customer.domain.model.Customer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepositoryTest {

    private InMemoryCustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
    }

    @Test
    void save_assignsValidId() {
        Customer saved = repository.save(new Customer(null, "Juan", "juan@email.com"));
        assertNotNull(saved.id());
        assertTrue(saved.id() > 0);
    }

    @Test
    void save_consecutiveIdsDoNotRepeat() {
        Customer first = repository.save(new Customer(null, "A", "a@email.com"));
        Customer second = repository.save(new Customer(null, "B", "b@email.com"));
        assertNotEquals(first.id(), second.id());
    }

    @Test
    void findById_existingCustomer_returnsCustomer() {
        Customer saved = repository.save(new Customer(null, "Juan", "juan@email.com"));
        var found = repository.findById(saved.id());
        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.get().id());
        assertEquals("Juan", found.get().name());
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        var found = repository.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsAllSavedCustomers() {
        repository.save(new Customer(null, "A", "a@email.com"));
        repository.save(new Customer(null, "B", "b@email.com"));

        List<Customer> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAll_doesNotExposeInternalCollection() {
        repository.save(new Customer(null, "A", "a@email.com"));
        List<Customer> all = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(new Customer(null, "X", "x@email.com")));
    }

    @Test
    void save_differentEmails_allSucceed() {
        Customer first = repository.save(new Customer(null, "A", "a@email.com"));
        Customer second = repository.save(new Customer(null, "B", "b@email.com"));
        assertNotNull(first.id());
        assertNotNull(second.id());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_duplicateEmail_throwsException() {
        repository.save(new Customer(null, "Juan", "juan@email.com"));
        assertThrows(DuplicateEmailException.class,
                () -> repository.save(new Customer(null, "Otro", "juan@email.com")));
    }

    @Test
    void save_emailNormalizationRespected() {
        repository.save(new Customer(null, "Juan", "JUAN@EMAIL.COM"));
        assertThrows(DuplicateEmailException.class,
                () -> repository.save(new Customer(null, "Otro", "juan@email.com")));
    }

    @Test
    void concurrent_differentEmails_generateUniqueIds() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            futures.add(executor.submit(() -> {
                latch.await();
                Customer saved = repository.save(
                        new Customer(null, "User" + index, "user" + index + "@email.com"));
                return saved.id();
            }));
        }

        latch.countDown();
        Set<Long> ids = new HashSet<>();
        for (Future<Long> future : futures) {
            ids.add(future.get(5, TimeUnit.SECONDS));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(threadCount, ids.size());
    }

    @Test
    void concurrent_sameEmail_onlyOneSucceeds() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                try {
                    repository.save(new Customer(null, "User", "same@email.com"));
                    return true;
                } catch (DuplicateEmailException e) {
                    return false;
                }
            }));
        }

        latch.countDown();
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(5, TimeUnit.SECONDS)) {
                successCount++;
            }
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successCount);
    }

    @Test
    void concurrent_consistencyMaintained() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            futures.add(executor.submit(() -> {
                latch.await();
                Customer saved = repository.save(
                        new Customer(null, "User" + index, "user" + index + "@test.com"));
                return saved.id();
            }));
        }

        latch.countDown();
        Set<Long> ids = new HashSet<>();
        for (Future<Long> future : futures) {
            ids.add(future.get(5, TimeUnit.SECONDS));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(threadCount, ids.size());

        List<Customer> all = repository.findAll();
        assertEquals(threadCount, all.size());

        for (Long id : ids) {
            var found = repository.findById(id);
            assertTrue(found.isPresent());
            assertEquals(id, found.get().id());
        }
    }
}
