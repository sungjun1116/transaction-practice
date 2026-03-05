package com.hanwha.transactionpractice.practice3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [케이스 4] noRollbackFor → RuntimeException이어도 커밋
 * =====================================================
 *
 * noRollbackFor 로 특정 RuntimeException을 롤백에서 제외할 수 있다.
 *
 * 사용 사례:
 *   잔액 부족은 비즈니스 상 예측 가능한 상황이다.
 *   "결제 시도 기록(Payment)은 DB에 남기되, 예외는 호출자에게 전달하고 싶을 때"
 *   → noRollbackFor 로 RuntimeException이어도 커밋을 허용한다.
 *
 * 동작 흐름:
 *   1. TX 시작
 *   2. Payment 저장
 *   3. InsufficientBalanceException(RuntimeException) 발생
 *   4. Spring AOP: noRollbackFor=InsufficientBalanceException.class → 롤백 제외 → TX 커밋
 *   5. DB에 Payment가 남아있음 (의도적 커밋)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoRollbackForService {

    private final PaymentRepository paymentRepository;

    @Transactional(noRollbackFor = InsufficientBalanceException.class)
    public void pay(String description, int amount) {
        paymentRepository.save(new Payment(description, amount));
        log.info("[케이스 4] 결제 저장 완료, RuntimeException 발생 예정");

        // RuntimeException이지만 noRollbackFor 지정 → 커밋됨 (결제 시도 기록 보존)
        throw new InsufficientBalanceException("잔액 부족: " + amount + "원");
    }
}
