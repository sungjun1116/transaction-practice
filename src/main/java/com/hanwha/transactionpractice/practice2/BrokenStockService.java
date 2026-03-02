package com.hanwha.transactionpractice.practice2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [문제 코드] Lost Update
 * 일반 SELECT(공유 락 없음)로 읽은 후 수정하면,
 * 두 트랜잭션이 동시에 같은 값을 읽고 각각 차감 → 하나의 차감이 사라진다.
 *
 * 예: 재고 10, TX-A와 TX-B 동시에 5개씩 차감
 *     둘 다 10을 읽고 → 둘 다 5로 업데이트 → 최종 5 (기대값 0)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrokenStockService {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId) // 일반 SELECT
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        log.info("[BROKEN] 현재 재고: {}, 차감: {}", product.getStock(), quantity);
        product.decreaseStock(quantity);
    }
}
