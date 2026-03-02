package com.hanwha.transactionpractice.practice1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [학습 목표 1 해결 + 학습 목표 2 적용]
 * =====================================================
 *
 * Self-Invocation 해결:
 *   this.logAudit() 대신 외부 빈 auditLogService.log()를 호출한다.
 *   → Spring 프록시를 경유하므로 @Transactional(REQUIRES_NEW)이 정상 적용된다.
 *
 * REQUIRES_NEW 효과:
 *   → 감사 로그가 독립 TX에서 즉시 커밋된다.
 *   → 주문 TX가 롤백되어도 감사 로그는 유지된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixedOrderService {

    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void placeOrder(String product, int amount) {
        orderRepository.save(new Order(product, amount));

        // 외부 빈 호출 → Spring 프록시 경유 → REQUIRES_NEW 정상 적용
        // → 감사 로그가 독립 TX에서 즉시 커밋됨
        auditLogService.log("ORDER_CREATED", "상품: " + product);

        // 결제 처리 중 예외 발생
        throw new RuntimeException("결제 시스템 장애");
        // 주문 TX만 롤백 → 감사 로그는 이미 독립 TX에서 커밋되어 살아있음
    }
}
