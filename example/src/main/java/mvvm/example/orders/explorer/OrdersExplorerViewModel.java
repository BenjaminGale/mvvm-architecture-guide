package mvvm.example.orders.explorer;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.editor.EditOrderRequest;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.Comparator;

public class OrdersExplorerViewModel {

    private final ObjectProperty<Order> selectedOrder = new SimpleObjectProperty<>(this, "selectedOrder");
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    private final IntegerProperty ordersCount = new SimpleIntegerProperty(this, "ordersCount", 0);
    private final IntegerProperty overdueOrdersCount = new SimpleIntegerProperty(this, "overdueOrdersCount", 0);

    private final Action openOrderAction;

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host, ObservableList<StatusItemViewModel> statusItems) {
        this.service = service;
        this.host = host;

        this.openOrderAction = new Action(
            () -> host.showOrderDetails(new EditOrderRequest(selectedOrder.get().id())),
            selectedOrder.isNotNull()
        );

        statusItems.addAll(
            new StatusItemViewModel(ordersCount, LabelType.All_ORDERS),
            new StatusItemViewModel(overdueOrdersCount, LabelType.OVERDUE_ORDERS)
        );

        refresh();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyIntegerProperty ordersCountProperty() {
        return ordersCount;
    }

    public ReadOnlyIntegerProperty overdueOrdersCountProperty() {
        return overdueOrdersCount;
    }

    public void refresh() {
        var result = service
            .fetchAllOrders()
            .stream()
            .sorted(Comparator.comparing(Order::date).reversed())
            .toList();

        orders.setAll(result);

        var overdue = (int) result.stream().filter(Order::isOverdue).count();
        host.setPendingOrderCount(overdue);

        ordersCount.set(result.size());
        overdueOrdersCount.set(overdue);
    }

    public ObjectProperty<Order> selectedOrderProperty() {
        return selectedOrder;
    }

    public Action openOrderAction() {
        return openOrderAction;
    }
}
