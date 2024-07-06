package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.dto.UserBotStateDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class SendConfirmMessageCommand implements BotCommand {
    @Override
    public void execute(Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
                        OrderRepository orderRepository
                        ) {
        sendConfirmMessageCommand(update, telegramClient, daoTelegramBot);
    }
    private void sendConfirmMessageCommand(Update update, TelegramClient telegramClient, DaoTelegramBot daoTelegramBot){
        long chatId = update.getMessage().getChatId();
        String address = update.getMessage().getText();
        UserBotStateDto botStateDto = daoTelegramBot.getBotStateDto(String.valueOf(chatId));
        double amountXmrUserWantBuy = botStateDto.getQuantity();
        String message =
                "Время на оплату заказа 15 минут! \n" +
                        "\n" +
                        "Средства отправленные без заявки, возврату НЕ подлежат! Оплачивать вы должны ровно ту сумму, которая указана в заявке, иначе мы ваш платеж не найдем!  Все претензии по обмену принимаются в течении 24 часов. \n" +
                        "Обращаем внимание: средства вы должны отправлять только со своей личной карты. Администрация может потребовать верификацию документов клиента или задержать обмен для проверки других данных.\n" +
                        "\n" +
                        "Итого к оплате: "+ amountXmrUserWantBuy * 159 * 92 +" RUB \n" +
                        "\n" +
                        "ВНИМАТЕЛЬНО сверяйте адрес своего кошелька!\n" +
                        "\n" +
                        "После оплаты средства будут переведены на кошелек: " +address + "\n" +
                        "\n" +
                        " \n" +
                        "\n" +
                        "Если у вас есть вопрос, или возникли проблемы с оплатой, пишите поддержке: @BINGO_SUPPORT\n" +
                        "\n" +
                        "Вы согласны на обмен?";


        SendMessage sendMessage = SendMessage // Create a message object
                .builder()
                .chatId(chatId)
                .text(message)
                // Set the keyboard markup
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Согласен")
                                        .callbackData("/confirm")
                                        .build()
                                ), new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Отмена")
                                        .callbackData("Отмена")
                                        .build()
                                )

                        ))

                        .build())
                .build();
        try {

            daoTelegramBot.updateBotState(LastBotStateEnum.USER_TYPE_XMR_ADDRESS,
                    botStateDto.getChatId(),
                    botStateDto.getCurrency(),
                    botStateDto.getQuantity(),
                    botStateDto.getPaymentMethod(),
                    address,
                    botStateDto.getPrice_Xmr_Usd(),
                    botStateDto.getPrice_USD_RUB()
            );
            int lastMessageId = daoTelegramBot.getLastSentMessageId(chatId);
            if (lastMessageId != 0){
                DeleteMessage deleteMessage = DeleteMessage.builder().messageId(lastMessageId).chatId(chatId).build();
                telegramClient.execute(deleteMessage);
                log.info("Delete message build: {}", lastMessageId);
            }
            Message sentMessage = telegramClient.execute(sendMessage);
            daoTelegramBot.saveLastSentMessageId(sentMessage.getMessageId(), chatId);
        } catch (TelegramApiException exception) {
            throw new RuntimeException("Telegram API exception while start command", exception);
        }
    }
}
