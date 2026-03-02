package com.hanwha.transactionpractice.practice1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [학습 목표 2] REQUIRES_NEW 전파 수준
 * =====================================================
 *
 * Self-Invocation 해결책: 감사 로그를 별도 빈으로 분리하여 외부 호출로 변경.
 * 외부 빈 호출은 Spring 프록시를 경유하므로 REQUIRES_NEW가 정상 적용된다.
 *
 * REQUIRES_NEW 동작:
 *   1. 호출 시점에 기존(주문) TX를 일시 중단(suspend)한다.
 *   2. 새로운 독립 TX를 시작하여 감사 로그를 저장하고 즉시 커밋한다.
 *   3. 이후 주문 TX가 재개되었다가 롤백되어도,
 *      이미 커밋된 감사 로그 TX는 영향을 받지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String detail) {
        log.info("[감사로그] {} : {}", action, detail);
        auditLogRepository.save(new AuditLog(action, detail));
    }
}
