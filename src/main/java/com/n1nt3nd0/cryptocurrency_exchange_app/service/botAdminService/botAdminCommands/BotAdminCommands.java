package com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService.botAdminCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

public interface BotAdminCommands extends Serializable {
    void execute(Update update,
                 DaoTelegramBot daoTelegramBot,
                 TelegramClient telegramClient,
                 OrderRepository orderRepository
                 );
}
