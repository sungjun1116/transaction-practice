package com.hanwha.transactionpractice.practice3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [케이스 2] Checked Exception → 롤백 안 됨, 커밋됨! (흔한 실수)
 * =====================================================
 *
 * Spring @Transactional 은 Checked Exception(Exception 상속)이 발생해도
 * 기본적으로 롤백하지 않는다. 트랜잭션은 정상 커밋된다.
 *
 * 왜 이런 설계인가?
 *   Checked Exception은 "예상 가능한 비즈니스 예외"로 간주한다.
 *   예외를 던지더라도 "이미 처리된 데이터는 DB에 남기겠다"는 의도.
 *   (EJB 설계 철학에서 유래)
 *
 * 흔한 실수:
 *   "예외가 터졌으니 당연히 롤백되겠지" → 실제로는 커밋됨!
 *   → 데이터 불일치 버그로 이어질 수 있다.
 *
 * 동작 흐름:
 *   1. TX 시작
 *   2. Payment 저장
 *   3. ExternalApiException(Checked) 발생
 *   4. Spring AOP: Checked Exception → 롤백 대상 아님 → TX 커밋
 *   5. DB에 Payment가 남아있음 (주의!)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckedExceptionService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void pay(String description, int amount) throws ExternalApiException {
        paymentRepository.save(new Payment(description, amount));
        log.info("[케이스 2] 결제 저장 완료, CheckedException 발생 예정");

        // Checked Exception → 롤백 안 됨, 커밋됨!
        throw new ExternalApiException("외부 결제 API 연결 실패");
    }
}
