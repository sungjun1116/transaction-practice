package com.hanwha.transactionpractice.practice3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================
 * [케이스 3] rollbackFor=Exception.class → Checked Exception도 롤백
 * =====================================================
 *
 * rollbackFor 를 명시하면 Checked Exception도 롤백 대상으로 지정할 수 있다.
 * 케이스 2의 "Checked Exception이 커밋된다"는 함정을 해결하는 방법이다.
 *
 * 동작 흐름:
 *   1. TX 시작
 *   2. Payment 저장
 *   3. ExternalApiException(Checked) 발생
 *   4. Spring AOP: rollbackFor=Exception.class → 롤백 대상 → TX 롤백
 *   5. DB에 아무것도 남지 않음
 *
 * 실무 팁:
 *   - 모든 예외에서 롤백하려면 rollbackFor = Exception.class
 *   - 특정 Checked 예외만 롤백하려면 rollbackFor = ExternalApiException.class
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RollbackForService {

    private final PaymentRepository paymentRepository;

    @Transactional(rollbackFor = Exception.class)
    public void pay(String description, int amount) throws ExternalApiException {
        paymentRepository.save(new Payment(description, amount));
        log.info("[케이스 3] 결제 저장 완료, CheckedException 발생 예정");

        // Checked Exception이지만 rollbackFor=Exception.class → 롤백됨
        throw new ExternalApiException("외부 결제 API 연결 실패");
    }
}
