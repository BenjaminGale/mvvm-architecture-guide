package mvvm.example.orders.domain.queries;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderSummary(
    UUID id,
    String reference,
    String customerName,
    LocalDate createdDate,
    LocalDate plannedShipDate,
    String status,
    BigDecimal total,
    boolean isOverdue
) {}
