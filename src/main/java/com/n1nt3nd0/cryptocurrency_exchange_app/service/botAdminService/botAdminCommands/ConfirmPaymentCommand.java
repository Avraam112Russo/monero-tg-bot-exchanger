package com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService.botAdminCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.AdminTransactionDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrExchangeOrder;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrOrderStatus;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.toIntExact;

@Component
@Slf4j
public class ConfirmPaymentCommand implements BotAdminCommands {
    @Override
    public void execute(Update update,
                        DaoTelegramBot daoTelegramBot,
                        TelegramClient telegramClient,
                        OrderRepository orderRepository) {
        confirmPayment(update, daoTelegramBot, telegramClient, orderRepository);
    }


    private void confirmPayment(Update update, DaoTelegramBot daoTelegramBot, TelegramClient telegramClient, OrderRepository orderRepository) {

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        AdminTransactionDto adminTransactionDto = daoTelegramBot.getAdminTransactionDto(String.valueOf(messageId));
        String username = adminTransactionDto.getUsername();
        Optional<XmrExchangeOrder> mayBeUser = orderRepository.findOrderWithUser(username);
        XmrExchangeOrder order = mayBeUser.orElseThrow(() -> new RuntimeException("Order %s not found".formatted(username)));
        order.setOrderStatus(XmrOrderStatus.ADMIN_CONFIRMED_PAYMENT);
        orderRepository.save(order);
        Long adminChatId = update.getCallbackQuery().getMessage().getChatId();
        String chatId = adminTransactionDto.getChatId();
        String messageText = "Оплата успешно произведена.\n В ближайшие 5 минут средства будут отправлены на ваш кошелек," +
                "бот уведомит вас об этом. Пожалуйста, ожидайте. ";
        SendMessage sendMessage = SendMessage // Create a message object object
                .builder()
                .chatId(chatId)
                .text(messageText)
                .build();

        String responseToAdmin = "Платеж пользователя %s подтвержден.".formatted(username);
        EditMessageText new_message = EditMessageText.builder()
                .chatId(adminChatId)
                .messageId(toIntExact(messageId))
                .text(responseToAdmin)
                .build();
        try {


            telegramClient.execute(new_message);
            Message sentMessage = telegramClient.execute(sendMessage);
            int lastMessageId = daoTelegramBot.getLastSentMessageId(Long.valueOf(chatId));
            if (lastMessageId != 0){
                DeleteMessage deleteMessage = DeleteMessage.builder().messageId(lastMessageId).chatId(chatId).build();
                telegramClient.execute(deleteMessage);
                log.info("Delete message build: {}", lastMessageId);
            }
            daoTelegramBot.saveLastSentMessageId(sentMessage.getMessageId(), Long.valueOf(chatId));
        }catch (TelegramApiException exception){
            throw new RuntimeException("Error while executing confirm payment: ", exception);
        }
    }
}
