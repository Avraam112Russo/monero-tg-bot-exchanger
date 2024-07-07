package com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService.botAdminCommands.BotAdminCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotAdminServiceImpl implements BotAdminService {
    private final TelegramClient telegramClient;
    private final DaoTelegramBot daoTelegramBot;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    @Override
    public void directTextAdminCommands(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

        }
    }

    @Override
    public void directCallbackQueryAdminCommands(Update update) {
        if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            String BOT_ADMIN_COMMANDS = "bot_admin_commands";
            BotAdminCommands adminCommand  =(BotAdminCommands) redisTemplate.opsForHash().get(BOT_ADMIN_COMMANDS, callData);
            if (adminCommand != null){
                adminCommand.execute(update, daoTelegramBot, telegramClient, orderRepository);
            }
            else {
                throw new RuntimeException("Admin command not found");
            }
        }
    }
}
