package com.nutrishare.payment.domain;

public enum PaymentStatus {
    INIT,
    CONFIRMED,
    FAILED,
    REFUND_REQUESTED,
    REFUNDED
}
