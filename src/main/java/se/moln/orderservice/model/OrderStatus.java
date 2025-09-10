package se.moln.orderservice.model;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    CREATED, CONFIRMED, SHIPPED, CANCELLED, FAILED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            CREATED,   Set.of(CONFIRMED, CANCELLED, FAILED),
            CONFIRMED, Set.of(SHIPPED,   CANCELLED, FAILED),
            SHIPPED,   Set.of(),
            CANCELLED, Set.of(),
            FAILED,    Set.of()
    );

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }
}
