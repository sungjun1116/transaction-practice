package com.hanwha.transactionpractice.practice1;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p1_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;
    private int amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(String product, int amount) {
        this.product = product;
        this.amount = amount;
        this.status = OrderStatus.CREATED;
    }

    public enum OrderStatus {
        CREATED, COMPLETED, FAILED
    }
}
