package com.n1nt3nd0.cryptocurrency_exchange_app.entity;

import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NamedEntityGraph(name = "fetch_order_with_user",
        attributeNodes = {
                @NamedAttributeNode(value = "userTelegramBot")
        }
)
@Data
@ToString(exclude = "userTelegramBot")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_xmr_exchange_order")
public class XmrExchangeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "address")
    private String address;
    @Column(name = "xmr_quantity")
    private double xmrQuantity;
    @Column(name = "sum_to_pay_rub")
    private double sumToPayRub;
    @Column(name = "last_market_price_usd")
    private double lastMarketPriceUsd;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private XmrOrderStatus orderStatus;
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserTelegramBot userTelegramBot;
}
