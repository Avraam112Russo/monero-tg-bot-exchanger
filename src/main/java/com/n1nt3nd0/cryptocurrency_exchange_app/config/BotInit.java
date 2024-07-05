package com.n1nt3nd0.cryptocurrency_exchange_app.config;

import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.BuyXmrCommand;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.UserTypeQuantityXmr;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botService.TelegramBotService;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.StartCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotInit {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TelegramBotService telegramBot;
    @Value("${bot.token}")
    private String botToken;


    private final StartCommand startCommand;
    private final BuyXmrCommand buyXmrCommand;
    private final UserTypeQuantityXmr userTypeQuantityXmr;

    @PostConstruct
    public void init() {
        try {
         String BOT_COMMANDS = "bot_commands";
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/start", startCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/buy_monero", buyXmrCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/user_type_xmr_amount", userTypeQuantityXmr);
         TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(botToken, telegramBot);
        log.info("Bot initialized successfully!");
        }catch (TelegramApiException telegramApiException){
            throw new RuntimeException(telegramApiException.getMessage());
        }
    }
}
