package com.hanwha.transactionpractice.practice2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LostUpdateTest {

    @Autowired
    private BrokenStockService brokenStockService;

    @Autowired
    private FixedStockService fixedStockService;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        Product product = productRepository.save(new Product("노트북", 100));
        productId = product.getId();
    }

    @Test
    @DisplayName("[문제] 일반 SELECT: 동시 차감 시 Lost Update 발생 가능")
    void lostUpdate_broken() throws InterruptedException {
        int threadCount = 10;
        int decreasePerThread = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    brokenStockService.decreaseStock(productId, decreasePerThread);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Product product = productRepository.findById(productId).get();
        System.out.println("=== [문제] 기대 재고: " + (100 - threadCount * decreasePerThread));
        System.out.println("=== [문제] 실제 재고: " + product.getStock());
        // Lost Update로 인해 실제 재고 > 기대 재고일 수 있음
    }

    @Test
    @DisplayName("[해결] SELECT FOR UPDATE: 동시 차감 시에도 정확한 재고")
    void lostUpdate_fixed() throws InterruptedException {
        int threadCount = 10;
        int decreasePerThread = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    fixedStockService.decreaseStock(productId, decreasePerThread);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Product product = productRepository.findById(productId).get();
        int expected = 100 - threadCount * decreasePerThread;
        System.out.println("=== [해결] 기대 재고: " + expected);
        System.out.println("=== [해결] 실제 재고: " + product.getStock());
        assertThat(product.getStock()).isEqualTo(expected);
    }
}
