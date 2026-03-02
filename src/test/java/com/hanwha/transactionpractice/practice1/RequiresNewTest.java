package com.hanwha.transactionpractice.practice1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [학습 목표 2] REQUIRES_NEW 전파 수준 검증
 *
 * 외부 빈(AuditLogService)을 통해 프록시를 경유하면
 * @Transactional(REQUIRES_NEW)이 정상 적용됨을 증명한다.
 *
 * REQUIRES_NEW 동작 흐름:
 *   1. placeOrder() → 주문 TX 시작
 *   2. auditLogService.log() 호출 → 주문 TX 일시 중단, 감사 로그 TX 시작
 *   3. 감사 로그 저장 → 감사 로그 TX 커밋 (독립 완료)
 *   4. 주문 TX 재개 → 예외 발생 → 주문 TX 롤백
 *
 * 결과:
 *   - 주문: 0건 (롤백)
 *   - 감사 로그: 1건 (독립 TX에서 이미 커밋)
 */
@SpringBootTest
class RequiresNewTest {

    @Autowired
    private FixedOrderService fixedOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("외부 빈 호출로 REQUIRES_NEW 적용 → 주문 롤백되어도 감사 로그는 독립 TX에서 커밋 유지")
    void requiresNew_auditLog_survivesOrderRollback() {
        // when: 주문 처리 중 예외 발생
        assertThatThrownBy(() -> fixedOrderService.placeOrder("노트북", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 시스템 장애");

        // then: 주문은 롤백, 감사 로그는 독립 TX에서 이미 커밋되어 유지
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(auditLogRepository.findAll()).hasSize(1);

        System.out.println("=== [REQUIRES_NEW] 주문 수: " + orderRepository.count() + " → 롤백");
        System.out.println("=== [REQUIRES_NEW] 감사로그 수: " + auditLogRepository.count() + " → 독립 TX 커밋 유지");
        System.out.println("=== [REQUIRES_NEW] 감사로그: " + auditLogRepository.findAll().get(0).getDetail());
    }
}
