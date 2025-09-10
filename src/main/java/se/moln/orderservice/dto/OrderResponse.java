package se.moln.orderservice.dto;

import se.moln.orderservice.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime orderDate
) {}
