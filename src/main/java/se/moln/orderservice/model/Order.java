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
    private UUID userId;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    public void changeStatus(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException("Invalid status transition: " + this.status + " -> " + next);
        }
        this.status = next;
    }

    @PrePersist
    void prePersist() {
        if (status == null) status = OrderStatus.CREATED;
        if (orderDate == null) orderDate = LocalDateTime.now();
    }

}