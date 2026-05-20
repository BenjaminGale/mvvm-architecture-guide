package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.OrderStatus;

import java.time.LocalDate;

public record OrderHeaderSummary(
    LocalDate createdDate,
    OrderStatus status,
    Customer customer,
    LocalDate plannedShipDate,
    String reference
) {}
