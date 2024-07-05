package com.n1nt3nd0.cryptocurrency_exchange_app.service.botService;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.UserBotStateDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.BotCommand;
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
public class TelegramBotServiceImpl implements TelegramBotService {
    private final TelegramClient telegramClient;
    private final DaoTelegramBot daoTelegramBot;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            String message = update.getMessage().getText();
            try {

                BotCommand botCommands = (BotCommand) redisTemplate.opsForHash().get("bot_commands", message);
                if (botCommands != null){
                    botCommands.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate);
            }
            }catch (RuntimeException exception){
                log.error("error while sending command", exception);
            }


            if (isNumeric(message)) {
                UserBotStateDto botStateDto = daoTelegramBot.getBotStateDto(String.valueOf(chatId));

                if (botStateDto != null){
                    log.info(botStateDto.toString());
                }


                BotCommand command = (BotCommand) redisTemplate.opsForHash().get("bot_commands", "/user_type_xmr_amount");
                if (command != null) {
                    command.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate);
                }
            }

        }



        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null){
                String callBackData = update.getCallbackQuery().getData();
                BotCommand command = (BotCommand) redisTemplate.opsForHash().get("bot_commands", callBackData);
                if (command != null){
                    command.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate);
                } else {
                    log.info("Command not found");
                    throw new RuntimeException("Command not found");
                }
        }


    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
