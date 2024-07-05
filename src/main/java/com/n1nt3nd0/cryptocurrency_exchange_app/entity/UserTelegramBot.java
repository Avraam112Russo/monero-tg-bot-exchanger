package com.n1nt3nd0.cryptocurrency_exchange_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_users")
public class UserTelegramBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "chat_id")
    private String chatId;
    @Column(name = "username")
    private String username;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
