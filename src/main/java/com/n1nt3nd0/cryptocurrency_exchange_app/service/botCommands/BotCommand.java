package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

public interface BotCommand extends Serializable {
    void execute(Update update,
                 TelegramClient telegramClient,
                 UserRepository userRepository,
                 DaoTelegramBot daoTelegramBot,
                 RestTemplate restTemplate

    );
}
