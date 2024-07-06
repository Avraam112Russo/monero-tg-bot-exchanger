package com.n1nt3nd0.cryptocurrency_exchange_app.dao;

import com.n1nt3nd0.cryptocurrency_exchange_app.dto.UserBotStateDto;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.Currency;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.LastBotStateEnum;
import com.n1nt3nd0.cryptocurrency_exchange_app.entity.botEnum.PaymentMethod;
import com.n1nt3nd0.cryptocurrency_exchange_app.service.botCommands.BotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
@Slf4j
public class DaoTelegramBot implements Serializable {
    private final RedisTemplate<String, Object> redisTemplate;

    public void updateBotState(LastBotStateEnum lastBotStateEnum,
                               String chatId,
                               Currency currency,
                               double quantity,
                               PaymentMethod paymentMethod,
                               String address,
                               double price_Xmr_Usd,
                               double price_Usd_Rub

    ){
        String KEY = "USER_BOT_STATE";
        UserBotStateDto dto = UserBotStateDto.builder()
                .address("DEFAULT")
                .currency(Currency.DEFAULT)
                .quantity(0)
                .paymentMethod(PaymentMethod.DEFAULT)
                .chatId(chatId)
                .lastBotStateEnum(lastBotStateEnum)
                .build();

        if (currency != null){
            dto.setCurrency(currency);
        }
        if (quantity > 0){
            dto.setQuantity(quantity);
        }
        if (paymentMethod != null){
            dto.setPaymentMethod(paymentMethod);
        }
        if (address != null){
            dto.setAddress(address);
        }
        if (price_Xmr_Usd > 0){
            dto.setPrice_Xmr_Usd(price_Xmr_Usd);
        }

        if (price_Usd_Rub > 0){
            dto.setPrice_USD_RUB(price_Usd_Rub);
        }

        redisTemplate.opsForHash().put(KEY, chatId, dto);

    }
    public UserBotStateDto getBotStateDto(String chatId){
        String KEY = "USER_BOT_STATE";
        return (UserBotStateDto) redisTemplate.opsForHash().get(KEY, chatId);
    }
    public BotCommand getBotCommandByName(String commandName){
        String KEY = "bot_commands";
        BotCommand command =(BotCommand) redisTemplate.opsForHash().get(KEY, commandName);
        return command;
    }

    public void saveLastSentMessageId(Integer messageId, long chatId) {
        String KEY = "last_sent_message_id_for_delete";
        redisTemplate.opsForHash().put(KEY, chatId, messageId);
    }
    public int getLastSentMessageId(long chatId){
        String KEY = "last_sent_message_id_for_delete";
        Object lastMessageId = redisTemplate.opsForHash().get(KEY, chatId);
        if (lastMessageId != null){
            return (int) lastMessageId;
        }
        return 0;
    }
}
