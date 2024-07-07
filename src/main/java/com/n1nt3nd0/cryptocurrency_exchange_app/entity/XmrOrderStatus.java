package com.n1nt3nd0.cryptocurrency_exchange_app.entity;

public enum XmrOrderStatus {
    AWAITING_PAYMENT,
    USER_HAS_MADE_PAYMENT,
    ADMIN_CONFIRMED_PAYMENT,
    FUNDS_HAVE_BEEN_SENT,
    ADMIN_CANCELED_PAYMENT
}
