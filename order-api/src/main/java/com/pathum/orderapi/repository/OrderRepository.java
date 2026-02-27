package com.pathum.orderapi.repository;

import com.pathum.orderapi.entity.Order;
import com.pathum.orderapi.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByProductId(Long productId);
}