package com.n1nt3nd0.cryptocurrency_exchange_app.repository;

import com.n1nt3nd0.cryptocurrency_exchange_app.entity.XmrExchangeOrder;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<XmrExchangeOrder, Integer> {
    @EntityGraph(value = "fetch_order_with_user")
    @Query(value = "select order from XmrExchangeOrder order where order.userTelegramBot.username=:username")
    Optional<XmrExchangeOrder> findOrderWithUser(@Param("username")String username);

}
