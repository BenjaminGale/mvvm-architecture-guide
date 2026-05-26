package mvvm.example.orders.editor;

import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;

public class GetOrderEditorDataQuery {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public GetOrderEditorDataQuery(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    public OrderEditorData execute(OrderEditorRequest request) {
        if (request.isNew()) {
            return new OrderEditorData(Order.draft(), null);
        }
        var order = orderRepository.findById(request.orderId()).orElseThrow();
        var customer = order.customerId() != null
            ? customerRepository.findById(order.customerId()).orElse(null)
            : null;
        return new OrderEditorData(order, customer);
    }
}
