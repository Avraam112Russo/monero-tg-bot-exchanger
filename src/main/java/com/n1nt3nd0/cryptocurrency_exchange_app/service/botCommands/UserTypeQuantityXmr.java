package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.jayway.jsonpath.JsonPath;
import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.Currency;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserTypeQuantityXmr implements BotCommand{
    @Override
    public void execute(Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
    OrderRepository orderRepository
    ) {
userTypeQuantityXmr(update, telegramClient, userRepository, daoTelegramBot, restTemplate);
    }
    private void userTypeQuantityXmr(Update update,
                                     TelegramClient telegramClient,
                                     UserRepository userRepository,
                                     DaoTelegramBot daoTelegramBot,
                                     RestTemplate restTemplate) {
        Long chatId = update.getMessage().getChatId();
        double quantityXmr = Double.parseDouble(update.getMessage().getText());
        UriComponentsBuilder uriComponentsBuilder =
                UriComponentsBuilder.fromHttpUrl("https://api.blockchair.com/monero/stats");
        Object response = restTemplate.getForObject(uriComponentsBuilder.toUriString(), Object.class);
        String lastXmrMarketPrice = JsonPath.parse(response).read("$.context.market_price_usd", String.class);
        log.info(lastXmrMarketPrice);
        double marketPriceRub = Double.parseDouble(lastXmrMarketPrice) * 90; // 15.970, 283729378293

        double truncatedMarketPriceRub = BigDecimal.valueOf(marketPriceRub).setScale(3, RoundingMode.HALF_UP).doubleValue(); // 15.970, 283
        double checkOutSum = quantityXmr * marketPriceRub;
        double truncateCheckOutSum = Math.floor(checkOutSum * 100) / 100;
        String messageText = "Средний рыночный курс XMR: " + lastXmrMarketPrice + " USD, " + truncatedMarketPriceRub + " RUB\n" +
                "\n" +
                "Вы получите: " + quantityXmr + " xmr\n" +
                "\n" +
                "Ваш внутренний баланс кошелька: 17 руб.\n" +
                "\n" +
                "Для продолжения выберите Способ оплаты:";



        SendMessage sendMessage = SendMessage // Create a message object object
                .builder()
                .chatId(chatId)
                .text(messageText)
                // Set the keyboard markup
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Сбербанк (%s) RUB.".formatted(truncateCheckOutSum))
                                        .callbackData("SBER")
                                        .build()
                                )
//                                , new InlineKeyboardRow(InlineKeyboardButton
//                                        .builder()
//                                        .text("Тинькоф (%s) RUB.".formatted(truncateCheckOutSum))
//                                        .callbackData("T_BANK")
//                                        .build()
//                                ),
//                                new InlineKeyboardRow(InlineKeyboardButton
//                                        .builder()
//                                        .text("Альфа Банк (%s) RUB.".formatted(truncateCheckOutSum))
//                                        .callbackData("ALFA")
//                                        .build()
//                                )
                        ))
                        .build())
                .build();

        try {
            int lastMessageId = daoTelegramBot.getLastSentMessageId(chatId);
            if (lastMessageId != 0){
                DeleteMessage deleteMessage = DeleteMessage.builder().messageId(lastMessageId).chatId(chatId).build();
                telegramClient.execute(deleteMessage);
                log.info("Delete message build: {}", lastMessageId);
            }
            Message sentMessage = telegramClient.execute(sendMessage);
            daoTelegramBot.saveLastSentMessageId(sentMessage.getMessageId(), chatId);
            daoTelegramBot.updateBotState(
                    LastBotStateEnum.USER_TYPE_AMOUNT_XMR_WANT_BUY,
                    String.valueOf(chatId),
                    Currency.XMR,
                    quantityXmr,
                    null,
                    null,
                    Double.parseDouble(lastXmrMarketPrice),
                    90 // TODO get price USDRUB from stock exchange
            );            log.info("Update bot state: {}", daoTelegramBot.getBotStateDto(String.valueOf(chatId)));
        } catch (TelegramApiException exception) {
            throw new RuntimeException("Telegram API exception while user_type_quantity_zmr command", exception);
        }
    }
}
