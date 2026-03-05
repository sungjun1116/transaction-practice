package com.hanwha.transactionpractice.practice3;

/**
 * Unchecked Exception (RuntimeException 상속)
 * → Spring @Transactional 기본 동작: 롤백 대상
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
