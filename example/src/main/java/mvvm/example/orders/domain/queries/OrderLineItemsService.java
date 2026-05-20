package mvvm.example.orders.domain.queries;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.editor.EditOrderRequest;
import mvvm.example.orders.editor.lineitems.LineItemsService;
import mvvm.example.stock.domain.commands.DeleteStockAllocationsCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrderLineItemsService implements LineItemsService {

    private final OrderRepository orderRepository;
    private final GetLineItemSummariesQuery summariesQuery;
    private final DeleteStockAllocationsCommand deleteStockAllocationsCommand;

    public OrderLineItemsService(OrderRepository orderRepository, GetLineItemSummariesQuery summariesQuery, DeleteStockAllocationsCommand deleteStockAllocationsCommand) {
        this.orderRepository = orderRepository;
        this.summariesQuery = summariesQuery;
        this.deleteStockAllocationsCommand = deleteStockAllocationsCommand;
    }

    @Override
    public List<LineItem> fetchLineItems(EditOrderRequest request) {
        return request.isNew() ? List.of() : orderRepository.findById(request.orderId()).orElseThrow().lineItems();
    }

    @Override
    public CompletableFuture<List<LineItemSummary>> fetchSummaries(List<LineItem> items, String orderId) {
        return summariesQuery.execute(items, orderId);
    }

    @Override
    public void deleteLineItem(String productId, String orderId) {
        deleteStockAllocationsCommand.execute(productId, orderId);
    }
}
