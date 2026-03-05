package com.hanwha.transactionpractice.practice3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [실습 3] Exception 종류에 따른 트랜잭션 롤백 동작 검증
 *
 * Spring @Transactional 롤백 규칙:
 * ┌────────────────────────────────────────────────────────────────┐
 * │  예외 종류              기본 동작      오버라이드 방법           │
 * ├────────────────────────────────────────────────────────────────┤
 * │  RuntimeException      롤백           noRollbackFor로 커밋 가능  │
 * │  Error                 롤백           noRollbackFor로 커밋 가능  │
 * │  Checked Exception     커밋           rollbackFor로 롤백 가능    │
 * └────────────────────────────────────────────────────────────────┘
 */
@SpringBootTest
class ExceptionRollbackTest {

    @Autowired
    private DefaultRollbackService defaultRollbackService;

    @Autowired
    private CheckedExceptionService checkedExceptionService;

    @Autowired
    private RollbackForService rollbackForService;

    @Autowired
    private NoRollbackForService noRollbackForService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("[케이스 1] RuntimeException → 기본 동작: 자동 롤백")
    void runtimeException_rollsBack() {
        // when
        assertThatThrownBy(() -> defaultRollbackService.pay("노트북 구매", 1_500_000))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("잔액 부족");

        // then: RuntimeException → 롤백 → DB에 아무것도 없음
        long count = paymentRepository.count();
        assertThat(count).isZero();

        System.out.println("=== [케이스 1] RuntimeException");
        System.out.println("    결제 수: " + count + "건 → 롤백됨");
    }

    @Test
    @DisplayName("[케이스 2] Checked Exception → 기본 동작: 롤백 안 됨, 커밋!")
    void checkedException_doesNotRollback() {
        // when
        assertThatThrownBy(() -> checkedExceptionService.pay("노트북 구매", 1_500_000))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("외부 결제 API");

        // then: Checked Exception → 롤백 안 됨 → DB에 결제 기록이 남아있음!
        long count = paymentRepository.count();
        assertThat(count).isEqualTo(1);

        System.out.println("=== [케이스 2] Checked Exception (기본 동작)");
        System.out.println("    결제 수: " + count + "건 → 예외 발생했지만 커밋됨! (주의)");
    }

    @Test
    @DisplayName("[케이스 3] rollbackFor=Exception.class → Checked Exception도 롤백")
    void rollbackFor_rollsBackCheckedException() {
        // when
        assertThatThrownBy(() -> rollbackForService.pay("노트북 구매", 1_500_000))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("외부 결제 API");

        // then: rollbackFor=Exception.class → Checked Exception도 롤백 → DB에 없음
        long count = paymentRepository.count();
        assertThat(count).isZero();

        System.out.println("=== [케이스 3] rollbackFor=Exception.class");
        System.out.println("    결제 수: " + count + "건 → rollbackFor 덕분에 롤백됨");
    }

    @Test
    @DisplayName("[케이스 4] noRollbackFor → RuntimeException이어도 커밋 (결제 시도 기록 보존)")
    void noRollbackFor_commitsRuntimeException() {
        // when
        assertThatThrownBy(() -> noRollbackForService.pay("노트북 구매", 1_500_000))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("잔액 부족");

        // then: noRollbackFor → RuntimeException이어도 커밋 → DB에 결제 시도 기록 남음
        long count = paymentRepository.count();
        assertThat(count).isEqualTo(1);

        System.out.println("=== [케이스 4] noRollbackFor=InsufficientBalanceException");
        System.out.println("    결제 수: " + count + "건 → RuntimeException이지만 커밋됨 (의도적)");
    }
}
