package mvvm.example.orders.editor;

import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.queries.GetOrderAllocationsQuery;
import mvvm.example.stock.domain.StockRepository;

import java.util.Map;

public class GetOrderEditorDataQuery {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final GetOrderAllocationsQuery allocationsQuery;

    public GetOrderEditorDataQuery(OrderRepository orderRepository, CustomerRepository customerRepository, StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.allocationsQuery = new GetOrderAllocationsQuery(stockRepository);
    }

    public OrderEditorData execute(OrderEditorRequest request) {
        if (request.isNew()) {
            return new OrderEditorData(Order.draft(), null, Map.of());
        }
        var order = orderRepository.findById(request.orderId()).orElseThrow();
        var customer = order.customerId() != null
            ? customerRepository.findById(order.customerId()).orElse(null)
            : null;
        var allocations = allocationsQuery.execute(request.orderId());
        return new OrderEditorData(order, customer, allocations);
    }
}
