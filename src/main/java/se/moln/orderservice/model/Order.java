package se.moln.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @PrePersist
    void prePersist() {
        if (status == null) status = OrderStatus.CREATED;
        if (orderDate == null) orderDate = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = "ORD-" + java.time.LocalDate.now()
                    + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }
}
