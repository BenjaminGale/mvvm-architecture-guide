package mvvm.example.orders.explorer;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.orders.domain.queries.OrderSummary;
import mvvm.example.orders.editor.OrderEditorRequest;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrdersExplorerViewModel extends ExplorerViewModel<OrderSummary> {

    private final ReadOnlyIntegerWrapper ordersCount = new ReadOnlyIntegerWrapper(this, "ordersCount", 0);
    private final ReadOnlyIntegerWrapper overdueOrdersCount = new ReadOnlyIntegerWrapper(this, "overdueOrdersCount", 0);

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host) {
        this.service = service;
        this.host = host;

        items().addListener((ListChangeListener<OrderSummary>) _ -> {
            ordersCount.set(items().size());
            overdueOrdersCount.set((int) items().stream().filter(OrderSummary::isOverdue).count());
        });
    }

    public ReadOnlyIntegerProperty ordersCountProperty() {
        return ordersCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty overdueOrdersCountProperty() {
        return overdueOrdersCount.getReadOnlyProperty();
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
        host.showOrderDetails(OrderEditorRequest.forNewOrder());
    }

    @Override
    protected void editItem(OrderSummary summary) {
        host.showOrderDetails(OrderEditorRequest.of(summary.id()));
    }

}
