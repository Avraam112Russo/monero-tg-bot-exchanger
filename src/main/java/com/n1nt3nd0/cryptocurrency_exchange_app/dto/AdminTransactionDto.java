package com.n1nt3nd0.cryptocurrency_exchange_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminTransactionDto implements Serializable {
    private int id;
    private String messageId;
    private String chatId;
    private String username;
}
