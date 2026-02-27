package com.pathum.orderapi.service;

import com.pathum.orderapi.dto.request.OrderRequestDto;
import com.pathum.orderapi.dto.response.OrderResponseDto;
import com.pathum.orderapi.entity.OrderStatus;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getAllOrders();

    List<OrderResponseDto> getOrdersByStatus(OrderStatus status);

    OrderResponseDto updateOrderStatus(Long id, OrderStatus status);

    void cancelOrder(Long id);
}