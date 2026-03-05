package com.hanwha.transactionpractice.practice3;

/**
 * Checked Exception (Exception 상속)
 * → Spring @Transactional 기본 동작: 롤백 대상 아님 (커밋됨!)
 */
public class ExternalApiException extends Exception {

    public ExternalApiException(String message) {
        super(message);
    }
}
