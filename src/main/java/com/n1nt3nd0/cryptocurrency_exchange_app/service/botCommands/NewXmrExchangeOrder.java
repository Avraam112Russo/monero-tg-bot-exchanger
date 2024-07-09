package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.UserBotStateDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.UserTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrExchangeOrder;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrOrderStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.Math.toIntExact;

@Component
@Slf4j
public class NewXmrExchangeOrder implements BotCommand {
    @Override
    public void execute(Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
                        OrderRepository orderRepository
                        ) {
        newXmrExchangeOrder(update, telegramClient, userRepository, daoTelegramBot, orderRepository);
    }
    private void newXmrExchangeOrder(Update update,
                                     TelegramClient telegramClient,
                                     UserRepository userRepository,
                                     DaoTelegramBot daoTelegramBot,
                                     OrderRepository orderRepository

    ){
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String userName = update.getCallbackQuery().getMessage().getChat().getUserName();
        UserBotStateDto botStateDto = daoTelegramBot.getBotStateDto(String.valueOf(chatId));
        double quantityXmrOrder = botStateDto.getQuantity();
        double lastMarketPriceUsd = botStateDto.getPrice_Xmr_Usd();
        double sumToPayInRuble = quantityXmrOrder * lastMarketPriceUsd * 90; // TODO: to fetch currently price USDRUB usage rest api
        Optional<UserTelegramBot> mayBeUser = userRepository.findUserTelegramBotByUsername(update.getCallbackQuery().getMessage().getChat().getUserName());

        LocalDateTime orderCreatedAt = LocalDateTime.now();
        LocalDateTime orderExpiresAt = LocalDateTime.now().plusMinutes(1);


        if (orderRepository.findOrderWithUser(userName).isPresent()){
            throw new RuntimeException("Order already exists");
        }
        XmrExchangeOrder order = XmrExchangeOrder.builder()
                .paymentMethod(botStateDto.getPaymentMethod())
                .address(botStateDto.getAddress())
                .xmrQuantity(botStateDto.getQuantity())
                .lastMarketPriceUsd(lastMarketPriceUsd)
                .sumToPayRub(sumToPayInRuble)
                .userTelegramBot(mayBeUser.get())
                .createdAt(orderCreatedAt)
                .expiresAt(orderExpiresAt)
                .orderStatus(XmrOrderStatus.AWAITING_PAYMENT)
                .build();
        XmrExchangeOrder savedOrder = orderRepository.save(order);
        log.info("Order {} saved successfully.", savedOrder.toString());
        String text = "Время на оплату вашего заказа № " + savedOrder.getId() + " 15 минут!\n" +
                "\n" +
                "Средства отправленные без заявки, возврату НЕ подлежат! Оплачивать вы должны ровно ту сумму, которая указана в заявке, иначе мы ваш платеж не найдем!  Все претензии по обмену принимаются в течении 24 часов. \n" +
                "Обращаем внимание: средства вы должны отправлять только со своей личной карты. Администрация может потребовать верификацию документов клиента или задержать обмен для проверки других данных.\n" +
                "\n" +
                "Для зачисления " + quantityXmrOrder + " XMR, Вам надо оплатить: " +  sumToPayInRuble + " RUB\n" +
                "\n" +
                "Итого к оплате: " +  sumToPayInRuble + " RUB\n" +
                "\n" +
                "После оплаты средства будут переведены на кошелек XMR: " + botStateDto.getAddress() + "\n" +
                "\n" +
                "Если у вас есть вопрос, или возникли проблемы с оплатой, пишите поддержке: @BINGO_SUPPORT \n" +
                "\n" +
                "Реквизиты для оплаты - перевод СТРОГО на Сбербанк по номеру телефона: 89241560676";

        EditMessageText new_message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(toIntExact(messageId))
                .text(text)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(

                                        new InlineKeyboardRow(InlineKeyboardButton
                                                .builder()
                                                .text("Я оплатил" )
                                                .callbackData("/the_user_has_made_a_payment"  )
                                                .build()
                                        ),
                                        new InlineKeyboardRow(InlineKeyboardButton
                                                .builder()
                                                .text("Отмена")
                                                .callbackData("/Отмена" )
                                                .build()
                                        )
                                )
                        )
                        .build())
                .build();

        try {
            telegramClient.execute(new_message);

        } catch (TelegramApiException e) {
            log.error("error while USER_CREATED_NEW_XMR_EXCHANGE_ORDER: " + e.getMessage());
            throw new RuntimeException();
        }
    }
}
