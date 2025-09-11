package se.moln.orderservice.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.moln.orderservice.dto.OrderHistoryDto;

import java.util.List;

@Service
public class OrderHistoryService {

    private final JwtService jwtService;

    public OrderHistoryService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Mono<List<OrderHistoryDto>> getOrdersForUser(String token, int page, int size) {
        // Extract user ID from token
        return Mono.fromCallable(() -> jwtService.extractSubject(token))
                .map(userId -> {
                    // For now, return empty list - this would typically query the database
                    // based on the user ID and pagination parameters
                    return List.<OrderHistoryDto>of();
                });
    }
}