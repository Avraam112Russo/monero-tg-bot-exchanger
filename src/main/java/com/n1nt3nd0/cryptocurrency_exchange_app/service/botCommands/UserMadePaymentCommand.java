package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrExchangeOrder;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;
import java.util.UUID;

import static java.lang.Math.toIntExact;

@Component
@Slf4j
public class UserMadePaymentCommand implements BotCommand {
    @Override
    public void execute(
                        Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
                        OrderRepository orderRepository) {
        userMadePayment(update, telegramClient, userRepository, daoTelegramBot, restTemplate, orderRepository);
    }
    private void userMadePayment(Update update,
                                 TelegramClient telegramClient,
                                 UserRepository userRepository,
                                 DaoTelegramBot daoTelegramBot,
                                 RestTemplate restTemplate,
                                 OrderRepository orderRepository){
        String username = update.getCallbackQuery().getMessage().getChat().getUserName();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String answer =  "Идет проверка оплаты. Пожалуйста, ожидайте. \n" +
                "\n" +
                "Будьте внимательны! \n" +
                "Если вы отправили неверную сумму или выполнили платеж частично, " +
                "то средства будут безвовзратно утеряны.";;
        EditMessageText new_message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(toIntExact(messageId))
                .text(answer)
                .build();


        Optional<XmrExchangeOrder> mayBeOrderWithUser = orderRepository.findOrderWithUser(username);
        XmrExchangeOrder order = mayBeOrderWithUser.orElseThrow(() -> new RuntimeException("Order not found"));
        String paymentMethod = order.getPaymentMethod().name();
        String amountToBePaid = String.valueOf(order.getSumToPayRub());
        String xmrQuantity = String.valueOf(order.getXmrQuantity());
        String xmrAddress = order.getAddress();
        String orderId = String.valueOf(order.getId());
        String message = "The User has made a payment. Please, check the transaction and send the coins. \n" +
                "\n" +
                "Payment method: " + paymentMethod + "\n" +
                "Amount to be paid: " + amountToBePaid + "\n" +
                "Username: " + username + "\n" +
                "XMR quantity: " + xmrQuantity + "\n" +
                "Xmr address: " + xmrAddress + "\n" +
                "Order id: " + orderId +
                "";



        SendMessage SEND_MESSAGE_TO_ADMIN = SendMessage // Create a message object object
                .builder()
                .chatId(7319257049L)
//                // Set the keyboard markup
//                .replyMarkup(InlineKeyboardMarkup
//                        .builder()
//                        .keyboard(List.of(
//                                new InlineKeyboardRow(InlineKeyboardButton
//                                        .builder()
//                                        .text("Подтвердить платеж")
//                                        .callbackData("/confirmPaymentAdminCommand")
//                                        .build()
//                                ), new InlineKeyboardRow(InlineKeyboardButton
//                                        .builder()
//                                        .text("Платеж не найден")
//                                        .callbackData("Платеж не найден")
//                                        .build()
//                                )
//                        ))
//                        .build())
                .text(message)
                // Set the keyboard markup
                .build();


        try {
            telegramClient.execute(new_message);
            telegramClient.execute(SEND_MESSAGE_TO_ADMIN);
            log.info("Send message to admin with successfully! ");
        } catch (TelegramApiException e) {
            log.error("error while update has callback query: " + e.getMessage());
            throw new RuntimeException();
        }
    }
}
