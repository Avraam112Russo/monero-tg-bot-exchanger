package com.n1nt3nd0.cryptocurrency_exchange_app.repository;

import com.n1nt3nd0.cryptocurrency_exchange_app.entity.UserTelegramBot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTelegramBot, Long> {
    Optional<UserTelegramBot> findUserTelegramBotByUsername(String username);
}
