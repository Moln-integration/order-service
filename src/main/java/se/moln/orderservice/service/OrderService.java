package se.moln.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.moln.orderservice.dto.AdjustStockRequest;
import se.moln.orderservice.dto.OrderRequest;
import se.moln.orderservice.dto.ProductResponse;
import se.moln.orderservice.dto.UserResponse;
import se.moln.orderservice.model.Order;
import se.moln.orderservice.model.OrderItem;
import se.moln.orderservice.model.OrderStatus;
import se.moln.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Value("${userservice.url}")
    private String userServiceUrl;

    @Value("${productservice.url}")
    private String productServiceUrl;

    @Transactional
    public Mono<Order> createOrder(OrderRequest orderRequest) {

        if (orderRequest.items() == null || orderRequest.items().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Items list must not be empty"));
        }

        return webClient.get()
                .uri(userServiceUrl + "/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + orderRequest.jwtToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.createException().flatMap(Mono::error))
                .bodyToMono(UserResponse.class)
                .flatMap(user -> {
                    UUID userId = user.userId();

                    Order order = new Order();
                    order.setUserId(userId);
                    order.setOrderDate(LocalDateTime.now());
                    order.setStatus(OrderStatus.CREATED);
                    order.setTotalAmount(BigDecimal.ZERO);

                    return Flux.fromIterable(orderRequest.items())
                            .flatMap(itemReq ->
                                    webClient.post()
                                            .uri(productServiceUrl + "/api/inventory/{productId}/purchase", itemReq.productId())
                                            .bodyValue(new AdjustStockRequest(itemReq.quantity()))
                                            .retrieve()
                                            .onStatus(HttpStatusCode::isError, resp -> resp.createException().flatMap(Mono::error))
                                            .bodyToMono(ProductResponse.class)
                                            .map(prod -> {
                                                OrderItem oi = new OrderItem();
                                                oi.setProductId(itemReq.productId());
                                                oi.setQuantity(itemReq.quantity());
                                                oi.setPriceAtPurchase(prod.price());
                                                oi.setProductName(prod.name());
                                                oi.setOrder(order);
                                                return oi;
                                            })
                            )
                            .collectList()
                            .doOnNext(items -> {
                                order.setOrderItems(items);
                                BigDecimal total = items.stream()
                                        .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                order.setTotalAmount(total);
                                order.setStatus(OrderStatus.CONFIRMED);
                            })

                            .flatMap(items ->
                                    Mono.fromCallable(() -> orderRepository.save(order))
                                            .subscribeOn(Schedulers.boundedElastic())
                            )

                            .onErrorResume(ex -> {
                                log.warn("Order creation failed, marking as FAILED", ex);
                                order.setStatus(OrderStatus.FAILED);
                                return Mono.fromCallable(() -> orderRepository.save(order))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then(Mono.error(ex));
                            });
                });
    }
}
