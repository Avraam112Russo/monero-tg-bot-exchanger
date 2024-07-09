package com.n1nt3nd0.cryptocurrency_exchange_app.config;

import com.n1nt3nd0.cryptocurrency_exchange_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 1000 * 61) // 1 minutes
    public void scheduleFixedRateTask() {

        orderRepository.findAll().stream()
                .forEach(order -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(order.getExpiresAt())){
                        orderRepository.delete(order);
                        log.info("Deleted order successfully: {}", order.toString());
                    }
                });
    }

}
