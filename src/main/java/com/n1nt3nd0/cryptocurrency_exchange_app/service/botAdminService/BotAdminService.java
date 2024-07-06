package com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotAdminService {
    void directTextAdminCommands(Update update);
    void directCallbackQueryAdminCommands(Update update);
}
