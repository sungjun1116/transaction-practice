package com.hanwha.transactionpractice.practice1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [학습 목표 1] Self-Invocation 문제 검증
 *
 * this.logAudit() 호출이 Spring AOP 프록시를 우회하여
 * @Transactional(REQUIRES_NEW) 선언이 무시됨을 증명한다.
 *
 * 기대 동작 (REQUIRES_NEW가 정상 작동했다면):
 *   - 감사 로그: 1건 (독립 TX에서 커밋)
 *   - 주문: 0건 (롤백)
 *
 * 실제 동작 (Self-Invocation으로 인해):
 *   - 감사 로그: 0건 ← REQUIRES_NEW 무시, 주문 TX와 같은 TX에서 롤백
 *   - 주문: 0건 (롤백)
 */
@SpringBootTest
class SelfInvocationTest {

    @Autowired
    private BrokenOrderService brokenOrderService;

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
    @DisplayName("this.logAudit() 호출 시 REQUIRES_NEW가 무시되어 주문 롤백 시 감사 로그도 함께 롤백")
    void selfInvocation_requiresNew_ignored() {
        // when: 주문 처리 중 예외 발생
        assertThatThrownBy(() -> brokenOrderService.placeOrder("노트북", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 시스템 장애");

        // then: REQUIRES_NEW가 무시되어 감사 로그가 주문 TX에 참여 → 함께 롤백
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(auditLogRepository.findAll()).isEmpty(); // REQUIRES_NEW였다면 1건이어야 함!

        System.out.println("=== [Self-Invocation] 주문 수: " + orderRepository.count());
        System.out.println("=== [Self-Invocation] 감사로그 수: " + auditLogRepository.count()
                + " (기대: 1, 실제: 0 → REQUIRES_NEW 무시됨)");
    }
}
