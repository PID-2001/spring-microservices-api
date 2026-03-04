package com.pathum.orderapi.service.impl;

import com.pathum.orderapi.client.ProductClient;
import com.pathum.orderapi.dto.request.OrderRequestDto;
import com.pathum.orderapi.dto.response.OrderResponseDto;
import com.pathum.orderapi.dto.response.ProductResponseDto;
import com.pathum.orderapi.entity.Order;
import com.pathum.orderapi.entity.OrderStatus;
import com.pathum.orderapi.exception.OrderNotFoundException;
import com.pathum.orderapi.mapper.OrderMapper;
import com.pathum.orderapi.repository.OrderRepository;
import com.pathum.orderapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        log.info("Creating order for productId: {}", requestDto.getProductId());

        ProductResponseDto product = productClient
                .getProductById(requestDto.getProductId());

        if (product.getQuantity() < requestDto.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.getQuantity()
            );
        }

        Order order = orderMapper.toEntity(
                product.getId(),
                product.getName(),
                requestDto.getQuantity(),
                product.getPrice()
        );

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with id: {}", savedOrder.getId());
        return orderMapper.toResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + id
                ));

        return orderMapper.toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        log.info("Fetching all orders");

        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);

        return orderRepository.findByStatus(status)
                .stream()
                .map(orderMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order status to {} for order id: {}", status, id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + id
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot update a cancelled order"
            );
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for id: {}", id);
        return orderMapper.toResponseDto(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + id
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Order is already cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled successfully with id: {}", id);
    }
}