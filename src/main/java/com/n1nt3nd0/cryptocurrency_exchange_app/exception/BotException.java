//package com.n1nt3nd0.cryptocurrency_exchange_app.exception;
//
//import com.n1nt3nd0.cryptocurrency_exchange_app.service.botService.TelegramBotServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//@ControllerAdvice(basePackageClasses = TelegramBotServiceImpl.class)
//@Slf4j
//public class BotException {
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> handleEx(RuntimeException e){
//      log.error("Exception handler working" +e.getMessage());
//    }
//}
