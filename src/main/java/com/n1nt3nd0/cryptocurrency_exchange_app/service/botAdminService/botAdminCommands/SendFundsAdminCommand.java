package com.n1nt3nd0.cryptocurrency_exchange_app.service.botAdminService.botAdminCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.AdminTransactionDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrExchangeOrder;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrOrderStatus;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
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
public class SendFundsAdminCommand implements BotAdminCommands {
    @Override
    public void execute(Update update, DaoTelegramBot daoTelegramBot, TelegramClient telegramClient, OrderRepository orderRepository) {
        sendFunds(update, daoTelegramBot, telegramClient, orderRepository);
    }
    private void sendFunds(Update update, DaoTelegramBot daoTelegramBot, TelegramClient telegramClient, OrderRepository orderRepository){
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        AdminTransactionDto adminTransactionDto = daoTelegramBot.getAdminTransactionDto(String.valueOf(messageId));
        String username = adminTransactionDto.getUsername();
        String chatId = adminTransactionDto.getChatId();

        Optional<XmrExchangeOrder> mayBeOrder = orderRepository.findOrderWithUser(username);
        XmrExchangeOrder order = mayBeOrder.orElseThrow(() -> new RuntimeException("Order %s not found".formatted(username)));
        order.setOrderStatus(XmrOrderStatus.FUNDS_HAVE_BEEN_SENT);
        orderRepository.save(order);
        String messageText = "Ваша заявка исполнена № " + order.getId() + "\n" +
                "  \n" +
                "На ваш кошелек:" + order.getAddress() + "\n" +
                "\n" +
                "Отправлена сумма:" + order.getXmrQuantity() + "\n" +
                "\n" +
                "Все обмены зачисляются после 5-15 подтверждений от сети, всё зависит от вашего сервиса, на котором расположен ваш кошелек.\n" +
                "\n" +
                "Время зачисления XMR при обычной загруженности в среднем от 20 минут.\n" +
                "\n" +
                "Процедура подтверждения НЕ ЗАВИСИТ от нас и определяется исключительно скоростью обработки транзакций криптовалютными сетями.\n" +
                "\n" +
                "\n" +
                "Спасибо за обмен! Вам начислилось 17 руб. на внутренний баланс.";
        Long adminChatId = update.getCallbackQuery().getMessage().getChatId();
        String responseToAdmin = ("Средства пользователя %s отправлены").formatted(username);

        SendMessage sendMessage = SendMessage // Create a message object object
                .builder()
                .chatId(chatId)
                .text(messageText)
                .build();

        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(adminChatId)
                .messageId(toIntExact(messageId))
                .text(responseToAdmin)
                .build();

        try {


            telegramClient.execute(editMessageText);
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
