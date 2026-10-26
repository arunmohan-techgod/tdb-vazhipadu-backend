package com.temple.vazhipadu_booking.order;

import com.temple.vazhipadu_booking.offering.Offering;
import com.temple.vazhipadu_booking.offering.OfferingRepository;
import com.temple.vazhipadu_booking.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DailyLimitConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OfferingRepository offeringRepository;

    @Test
    void exactlyFiveOfTwentySimultaneousOrdersShouldSucceed()
            throws Exception {

        Offering offering = new Offering(
            "Concurrency Test Offering",
            new BigDecimal("100.00"),
            true
        );
        offering.setDailyLimit(5);
        offering = offeringRepository.saveAndFlush(offering);

        Long offeringId = offering.getId();
        LocalDate date = LocalDate.of(2027, 1, 15);

        int requestCount = 20;
        int dailyLimit = 5;

        ExecutorService executor =
            Executors.newFixedThreadPool(requestCount);

        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();

        try {
            List<Future<?>> futures =
                new java.util.ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                final int requestNumber = i;

                futures.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                            "Timed out waiting to start"
                        );
                    }

                    CreateOrderRequest request =
                        new CreateOrderRequest(
                            "Devotee " + requestNumber,
                            "900000%05d".formatted(requestNumber),
                            "Ashwini",
                            date,
                            List.of(
                                new CreateOrderRequest.OrderItemRequest(
                                    offeringId, 1
                                )
                            )
                        );

                    try {
                        orderService.create(request);
                        successes.incrementAndGet();
                    } catch (IllegalArgumentException ex) {
                        // Expected when the daily limit is reached.
                        rejections.incrementAndGet();
                    }

                    return null;
                }));
            }

            assertTrue(
                ready.await(10, TimeUnit.SECONDS),
                "All requests should be ready"
            );

            start.countDown();

            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }

            assertEquals(dailyLimit, successes.get(),
                "Exactly five orders should succeed");

            assertEquals(requestCount - dailyLimit, rejections.get(),
                "The other fifteen orders should be rejected");
        } finally {
            executor.shutdownNow();
        }
    }
}

