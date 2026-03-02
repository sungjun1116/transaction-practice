package com.hanwha.transactionpractice.practice1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [학습 목표 1] Self-Invocation
 * =====================================================
 *
 * 개발자 의도:
 *   this.logAudit()에 @Transactional(REQUIRES_NEW)을 선언하여
 *   주문 TX와 독립된 트랜잭션에서 감사 로그를 기록하려 했다.
 *
 * 왜 실패하는가:
 *   Spring @Transactional은 AOP 프록시 기반으로 동작한다.
 *   외부에서 빈을 호출할 때만 프록시를 경유하고,
 *   같은 클래스 내부에서 this.method()를 호출하면 프록시를 우회한다.
 *   → @Transactional(REQUIRES_NEW) 선언이 완전히 무시된다.
 *   → logAudit()이 주문 TX에 그대로 참여한다.
 *   → 주문 롤백 시 감사 로그도 함께 사라진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrokenOrderService {

    private final OrderRepository orderRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void placeOrder(String product, int amount) {
        orderRepository.save(new Order(product, amount));

        // this.logAudit() → 프록시를 거치지 않으므로 REQUIRES_NEW 무시!
        // → 주문 TX와 동일한 TX에서 감사 로그가 기록됨
        this.logAudit("ORDER_CREATED", "상품: " + product);

        // 결제 처리 중 예외 발생
        throw new RuntimeException("결제 시스템 장애");
        // 전체 TX 롤백 → 감사 로그도 함께 사라짐 (REQUIRES_NEW가 무시됐으므로)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(String action, String detail) {
        // Self-Invocation 시: REQUIRES_NEW가 무시되어 주문 TX에 그대로 참여
        log.info("[감사로그] {} : {}", action, detail);
        auditLogRepository.save(new AuditLog(action, detail));
    }
}
