package mvvm.example.orders.explorer;

import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.requests.EditOrderRequest;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrdersExplorerViewModel extends ExplorerViewModel<Order> {

    private final IntegerProperty ordersCount = new SimpleIntegerProperty(this, "ordersCount", 0);
    private final IntegerProperty overdueOrdersCount = new SimpleIntegerProperty(this, "overdueOrdersCount", 0);

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host, ObservableList<StatusItemViewModel> statusItems) {
        this.service = service;
        this.host = host;

        items().addListener((ListChangeListener<Order>) _ -> {
            var list = items().stream().toList();
            ordersCount.set(list.size());
            overdueOrdersCount.set((int) list.stream().filter(Order::isOverdue).count());
        });

        statusItems.addAll(
            new StatusItemViewModel(ordersCount, LabelType.All_ORDERS),
            new StatusItemViewModel(overdueOrdersCount, LabelType.OVERDUE_ORDERS)
        );
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
    protected CompletableFuture<List<Order>> fetchItemsAsync() {
        return CompletableFuture.completedFuture(
            service.fetchAllOrders()
                .stream()
                .sorted(Comparator.comparing(Order::createdDate).reversed())
                .toList()
        );
    }

    @Override
    protected void addItem() {
        host.showOrderDetails(EditOrderRequest.forNewOrder());
    }

    @Override
    protected void editItem(Order order) {
        host.showOrderDetails(EditOrderRequest.of(order.id()));
    }

    @Override
    protected void deleteItem(Order order) {
        throw new UnsupportedOperationException();
    }
}
