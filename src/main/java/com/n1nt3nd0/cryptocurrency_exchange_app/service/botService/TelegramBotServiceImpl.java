package com.n1nt3nd0.cryptocurrency_exchange_app.service.botService;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService.BotAdminService;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.BotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotServiceImpl implements TelegramBotService {
    private final TelegramClient telegramClient;
    private final DaoTelegramBot daoTelegramBot;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final BotAdminService botAdminService;
    @Override
    public void consume(Update update) {
        if (update.hasMessage()
                && update.getMessage().hasText()
                && update.getMessage().getChatId() ==  7319257049L
                && update.getMessage().getChat().getUserName().equals("ep1ct3t")) {

            botAdminService.directTextAdminCommands(update);

        }
//        if (chatId == 7319257049L && userName.equals("ep1ct3t")){
//        }
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getChatId() != 7319257049L) {


            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            String message = update.getMessage().getText();






                BotCommand botCommands = daoTelegramBot.getBotCommandByName(message);
                if (botCommands != null){
                    botCommands.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate, orderRepository);
            } else {
                    log.error("BotCommand not found");
                }


            if (isNumeric(message)) {
                BotCommand command = daoTelegramBot.getBotCommandByName("/user_type_xmr_amount");
                if (command != null) {
                    command.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate, orderRepository);
                }
            }

            if (message.length() > 40 && daoTelegramBot.getBotStateDto(String.valueOf(chatId)).getLastBotStateEnum().name().equals("USER_CHOOSE_PAYMENT_METHOD")){
                BotCommand botCommandByName = daoTelegramBot.getBotCommandByName("/user_type_xmr_address");
                if (botCommandByName != null) {
                    botCommandByName.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate, orderRepository);
                }
            }

        }



        if (update.hasCallbackQuery() &&
                update.getCallbackQuery().getMessage().getChatId() == 7319257049L
                && update.getCallbackQuery().getMessage().getChat().getUserName().equals("ep1ct3t")){
            botAdminService.directCallbackQueryAdminCommands(update);
        }
        if (update.hasCallbackQuery()
                && update.getCallbackQuery().getData() != null
                && update.getCallbackQuery().getMessage().getChatId() != 7319257049L ){
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String userName = update.getCallbackQuery().getMessage().getChat().getUserName();


                String callBackData = update.getCallbackQuery().getData();
                BotCommand command = daoTelegramBot.getBotCommandByName(callBackData);
                if (command != null){
                    command.execute(update, telegramClient, userRepository, daoTelegramBot, restTemplate, orderRepository);
                }
                else {
                    log.info("Command not found");
                    throw new RuntimeException("Command not found");
                }
        }


    }

    private boolean isNumeric(String strNum) {
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
