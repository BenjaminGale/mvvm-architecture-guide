package mvvm.example.orders.domain.queries;

import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.editor.EditOrderRequest;
import mvvm.example.orders.editor.header.OrderHeaderService;
import mvvm.example.orders.editor.header.OrderHeaderSummary;

import java.time.LocalDate;

public class GetOrderHeaderSummaryQuery implements OrderHeaderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public GetOrderHeaderSummaryQuery(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public OrderHeaderSummary fetch(EditOrderRequest request) {
        if (request.isNew()) {
            return new OrderHeaderSummary(LocalDate.now(), OrderStatus.PENDING, null, LocalDate.now(), "");
        }
        var order = orderRepository.findById(request.orderId()).orElseThrow();
        var customer = order.customerId() != null
            ? customerRepository.findById(order.customerId()).orElse(null)
            : null;
        return new OrderHeaderSummary(order.createdDate(), order.status(), customer, order.plannedShipDate(), order.reference());
    }
}
