package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSummary(
    String id,
    String reference,
    String customerName,
    LocalDate createdDate,
    LocalDate plannedShipDate,
    String status,
    BigDecimal total,
    boolean isOverdue
) {}
