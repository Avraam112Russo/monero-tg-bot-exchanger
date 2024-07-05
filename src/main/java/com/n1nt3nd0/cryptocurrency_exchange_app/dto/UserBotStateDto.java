package com.n1nt3nd0.cryptocurrency_exchange_app.dto;


import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.Currency;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBotStateDto implements Serializable {
    private String chatId;
    private LastBotStateEnum lastBotStateEnum;
    private Currency currency;
    private double quantity;
    private PaymentMethod paymentMethod;
    private String address;
}
