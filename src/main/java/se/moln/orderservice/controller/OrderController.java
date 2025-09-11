package se.moln.orderservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import se.moln.orderservice.dto.OrderHistoryDto;
import se.moln.orderservice.dto.OrderRequest;
import se.moln.orderservice.model.Order;
import se.moln.orderservice.service.OrderHistoryService;
import se.moln.orderservice.service.OrderService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    public OrderController(OrderService orderService, OrderHistoryService orderHistoryService) {
        this.orderService = orderService;
        this.orderHistoryService = orderHistoryService;
    }

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody OrderRequest req) {
        return orderService.createOrder(req)
                .map(newOrder -> ResponseEntity.status(HttpStatus.CREATED).body(newOrder));
    }
    @GetMapping("/history")
    public Mono<ResponseEntity<List<OrderHistoryDto>>> getOrderHistory(
            @RequestHeader(name= HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        if(authHeader == null || !authHeader.startsWith("Bearer")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .header("X-Error-Message", "Missing or invalid Authorization header")
                    .body(Collections.emptyList()));
        }
        String token = authHeader.substring(7);

        return orderHistoryService.getOrdersForUser(token, page, size)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header("X-Error-Message", ex.getMessage())
                                .body(Collections.emptyList())));
    }
}