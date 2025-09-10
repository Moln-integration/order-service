package se.moln.orderservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import se.moln.orderservice.dto.OrderRequest;
import se.moln.orderservice.dto.OrderResponse;
import se.moln.orderservice.model.Order;
import se.moln.orderservice.service.OrderService;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest req, UriComponentsBuilder uri) {
        return orderService.createOrder(req)
                .map(order -> {
                    URI location = uri.path("/api/orders/{id}")
                            .buildAndExpand(order.getId())
                            .toUri();
                    return ResponseEntity.created(location).body(toResponse(order));
                });
    }

    private OrderResponse toResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getStatus(),
                o.getOrderDate()
        );
    }
}
