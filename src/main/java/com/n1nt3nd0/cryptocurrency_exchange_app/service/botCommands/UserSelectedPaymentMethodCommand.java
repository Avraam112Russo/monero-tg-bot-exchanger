package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.UserBotStateDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.Currency;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.PaymentMethod;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static java.lang.Math.toIntExact;

@Component
@Slf4j
public class UserSelectedPaymentMethodCommand implements BotCommand {

    @Override
    public void execute(Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
                        OrderRepository orderRepository
                        ) {
        userSelectedPaymentMethodCommand(update, daoTelegramBot, telegramClient);
    }
    private void userSelectedPaymentMethodCommand(Update update, DaoTelegramBot daoTelegramBot, TelegramClient telegramClient){
        String username = update.getCallbackQuery().getMessage().getChat().getUserName();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();


        String answer = "Скопируйте и отправьте боту свой кошелек XMR";
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
            UserBotStateDto botStateDto = daoTelegramBot.getBotStateDto(String.valueOf(chatId));
            daoTelegramBot.updateBotState(
                    LastBotStateEnum.USER_CHOOSE_PAYMENT_METHOD,
                    botStateDto.getChatId(),
                    botStateDto.getCurrency(),
                    botStateDto.getQuantity(),
                    PaymentMethod.valueOf(callbackData),
                    null,
                    botStateDto.getPrice_Xmr_Usd(),
                    botStateDto.getPrice_USD_RUB()
            );
        } catch (TelegramApiException  | RuntimeException e) {
            log.error("error while userSelectedPaymentMethodCommand: " + e.getMessage());
            throw new RuntimeException();
        }
    }
}
