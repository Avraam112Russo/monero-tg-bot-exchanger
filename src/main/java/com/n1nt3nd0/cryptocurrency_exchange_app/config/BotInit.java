package com.n1nt3nd0.cryptocurrency_exchange_app.config;

import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.*;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botService.TelegramBotService;
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
    private final UserSelectedPaymentMethodCommand userSelectedPaymentMethodCommand;
    private final SendConfirmMessageCommand sendConfirmMessageCommand;
    private final NewXmrExchangeOrder newXmrExchangeOrder;
    private final UserMadePaymentCommand userMadePaymentCommand;
    @PostConstruct
    public void init() {
        try {
         String BOT_COMMANDS = "bot_commands";
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/start", startCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/buy_monero", buyXmrCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/user_type_xmr_amount", userTypeQuantityXmr);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "SBER", userSelectedPaymentMethodCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/user_type_xmr_address", sendConfirmMessageCommand);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/confirm", newXmrExchangeOrder);
         redisTemplate.opsForHash().put(BOT_COMMANDS, "/the_user_has_made_a_payment", userMadePaymentCommand);

         TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(botToken, telegramBot);
        log.info("Bot initialized successfully!");
        }catch (TelegramApiException telegramApiException){
            throw new RuntimeException(telegramApiException.getMessage());
        }
    }
}
