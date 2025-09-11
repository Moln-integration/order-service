package se.moln.orderservice.dto;

import java.time.LocalDateTime;

public record OrderHistoryDto(
        Long orderId,
        String productId,
        int quantity,
        LocalDateTime orderDate
) {
}