package mvvm.example.orders.explorer;

import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.orders.domain.queries.OrderSummary;
import mvvm.example.orders.editor.EditOrderRequest;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrdersExplorerViewModel extends ExplorerViewModel<OrderSummary> {

    private final IntegerProperty ordersCount = new SimpleIntegerProperty(this, "ordersCount", 0);
    private final IntegerProperty overdueOrdersCount = new SimpleIntegerProperty(this, "overdueOrdersCount", 0);

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host) {
        this.service = service;
        this.host = host;

        items().addListener((ListChangeListener<OrderSummary>) _ -> {
            var list = items().stream().toList();
            ordersCount.set(list.size());
            overdueOrdersCount.set((int) list.stream().filter(OrderSummary::isOverdue).count());
        });
    }

    public ReadOnlyIntegerProperty ordersCountProperty() {
        return ordersCount;
    }

    public ReadOnlyIntegerProperty overdueOrdersCountProperty() {
        return overdueOrdersCount;
    }

    @Override
    protected ObservableBooleanValue canDeleteCondition() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    protected CompletableFuture<List<OrderSummary>> fetchItemsAsync() {
        return service.fetchOrderSummaries().thenApply(list ->
            list.stream()
                .sorted(Comparator.comparing(OrderSummary::createdDate).reversed())
                .toList()
        );
    }

    @Override
    protected void addItem() {
        host.showOrderDetails(EditOrderRequest.forNewOrder());
    }

    @Override
    protected void editItem(OrderSummary summary) {
        host.showOrderDetails(EditOrderRequest.of(summary.id()));
    }

    @Override
    protected void deleteItem(OrderSummary summary) {
        throw new UnsupportedOperationException();
    }
}
