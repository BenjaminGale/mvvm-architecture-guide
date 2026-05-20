package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.editor.EditOrderRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LineItemsService {
    List<LineItem> fetchLineItems(EditOrderRequest request);
    CompletableFuture<List<LineItemSummary>> fetchSummaries(List<LineItem> items, String orderId);
    void deleteLineItem(String productId, String orderId);
}
