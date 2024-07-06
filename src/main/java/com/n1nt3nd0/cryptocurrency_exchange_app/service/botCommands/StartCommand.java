package com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands;

import com.n1nt3nd0.cryptocurrency_exchange_app.dao.DaoTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.UserTelegramBot;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import com.n1nt3nd0.cryptocurrency_exchange_app.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StartCommand implements BotCommand {

    @Override
    public void execute(Update update,
                        TelegramClient telegramClient,
                        UserRepository userRepository,
                        DaoTelegramBot daoTelegramBot,
                        RestTemplate restTemplate,
                        OrderRepository orderRepository
    ) {
        startCommand(update, telegramClient, userRepository, daoTelegramBot);
    }
    private void startCommand(Update update,
                              TelegramClient telegramClient,
                              UserRepository userRepository,
                              DaoTelegramBot daoTelegramBot ){
        String username = update.getMessage().getChat().getUserName();
        long chatId = update.getMessage().getChatId();
        int lastMessageId = daoTelegramBot.getLastSentMessageId(chatId);



        if (lastMessageId != 0){
            DeleteMessage deleteMessage = DeleteMessage.builder().messageId(lastMessageId).chatId(chatId).build();
            try {
            telegramClient.execute(deleteMessage);
            }catch (TelegramApiException exception){
                log.error("Error while delete message in start command: "+exception.getMessage());
            }
            log.info("Delete message build: {}", lastMessageId);
        }



        Optional<UserTelegramBot> mayBeUser = userRepository.findUserTelegramBotByUsername(username);
        if(mayBeUser.isEmpty()){
            UserTelegramBot userTelegramBot = UserTelegramBot.builder()
                    .username(username)
                    .chatId(String.valueOf(chatId))
                    .createdAt(LocalDateTime.now())
                    .build();
            UserTelegramBot savedUser = userRepository.save(userTelegramBot);
            log.info("User {} saved successfully.", savedUser.getUsername());
        }



        daoTelegramBot.updateBotState(LastBotStateEnum.START_COMMAND,
                String.valueOf(chatId),
                null,
                0,
                null,
                null,
                0,
                0

        );
        SendMessage sendMessage = SendMessage // Create a message object
                .builder()
                .chatId(chatId)
                .text("Бот обменник ✅\n" +
                        "\n" +
                        "Тут ты можешь обменять свои RUB на XMR\n" +
                        "\n" +
                        "Жми кнопку  Купить XMR или просто введи сумму в RUB или XMR\n" +
                        "\n" +
                        "Пример: 0.1 или 0,1 или 5030")
//                .text(answer)
                // Set the keyboard markup
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text(EmojiParser.parseToUnicode(" \uD83D\uDC49 Купить xmr \uD83D\uDC48"))
                                        .callbackData("/buy_monero")
                                        .build()
                                ), new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Партнерская программа")
                                        .callbackData("Партнерская программа")
                                        .build()
                                ),
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Поддержка / Оператор")
                                        .callbackData("Поддержка / Оператор")
                                        .build(), InlineKeyboardButton
                                        .builder()
                                        .text("Отзывы")
                                        .callbackData("Отзывы")
                                        .build()

                                )
                        ))
                        .build())
                .build();
        try {
            Message sentMessage = telegramClient.execute(sendMessage);
            daoTelegramBot.saveLastSentMessageId(sentMessage.getMessageId(), chatId);
        } catch (TelegramApiException exception) {
            throw new RuntimeException("Telegram API exception while start command", exception);
        }
    }
}
