package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.Currency;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.UUID;

import static java.lang.Math.toIntExact;
@Component
@Slf4j
public class BuyXmrCommand implements BotCommand{
    @Override
    public void execute(Update update, TelegramClient telegramClient, UserRepository userRepository, DaoTelegramBot daoTelegramBot, RestTemplate restTemplate) {
        buyXmrCommand(update, telegramClient, userRepository, daoTelegramBot);
    }
    @SneakyThrows
    private void buyXmrCommand(Update update, TelegramClient telegramClient, UserRepository userRepository, DaoTelegramBot daoTelegramBot) {

        String answer = "Укажите сумму в BTC или же RUB:\n" +
                "\n" +
                "Пример: 0.001 или 0,001 или 5030 ";
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        EditMessageText new_message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(toIntExact(messageId))
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Отмена")
                                        .callbackData("Отмена")
                                        .build()
                                )
                        )
                        .build())
                .build();
        try {
            telegramClient.execute(new_message);

            daoTelegramBot.updateBotState(LastBotStateEnum.BUY_XMR_COMMAND, String.valueOf(chatId), Currency.XMR, 0, null, null);
        } catch (TelegramApiException e) {
            log.error("error while execute BUY_XMR_COMMAND: " + e.getMessage());
            throw new RuntimeException();
        }
    }

}
