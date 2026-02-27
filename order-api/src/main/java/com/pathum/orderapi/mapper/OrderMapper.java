package com.pathum.orderapi.mapper;

import com.pathum.orderapi.dto.response.OrderResponseDto;
import com.pathum.orderapi.dto.response.ProductResponseDto;
import com.pathum.orderapi.entity.Order;
import com.pathum.orderapi.entity.OrderStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class OrderMapper {

    public Order toEntity(Long productId, String productName,
                          Integer quantity, BigDecimal unitPrice) {

        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return Order.builder()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .build();
    }

    public OrderResponseDto toResponseDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}