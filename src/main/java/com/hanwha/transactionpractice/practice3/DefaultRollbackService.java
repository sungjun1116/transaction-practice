package com.hanwha.transactionpractice.practice3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [케이스 1] RuntimeException → 자동 롤백 (Spring 기본 동작)
 * =====================================================
 *
 * Spring @Transactional 은 RuntimeException(Unchecked Exception) 발생 시
 * 트랜잭션을 자동으로 롤백한다.
 *
 * 동작 흐름:
 *   1. TX 시작
 *   2. Payment 저장 (아직 커밋 전)
 *   3. InsufficientBalanceException(RuntimeException) 발생
 *   4. Spring AOP가 예외를 감지 → TX 롤백
 *   5. DB에 아무것도 남지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRollbackService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void pay(String description, int amount) {
        paymentRepository.save(new Payment(description, amount));
        log.info("[케이스 1] 결제 저장 완료, RuntimeException 발생 예정");

        // RuntimeException(Unchecked) → 자동 롤백
        throw new InsufficientBalanceException("잔액 부족: " + amount + "원");
    }
}
