package com.hanwha.transactionpractice.practice2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [해결 코드] SELECT ... FOR UPDATE (비관적 락)
 * X-Lock을 획득하여 다른 트랜잭션이 같은 row를 동시에 읽지 못하게 한다.
 * → Lost Update 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixedStockService {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId) // SELECT ... FOR UPDATE
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        log.info("[FIXED] 현재 재고: {}, 차감: {}", product.getStock(), quantity);
        product.decreaseStock(quantity);
    }
}
