package mvvm.example.orders.explorer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.orders.domain.Order;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.Comparator;

public class OrdersExplorerViewModel {

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final IntegerProperty orderCount = new SimpleIntegerProperty(0);
    private final IntegerProperty overdueCount = new SimpleIntegerProperty(0);

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host, ObservableList<StatusItemViewModel> statusItems) {
        this.service = service;
        this.host = host;
        statusItems.add(new StatusItemViewModel(orderCount, LabelType.All_ORDERS));
        statusItems.add(new StatusItemViewModel(overdueCount, LabelType.OVERDUE_ORDERS));
        refresh();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyIntegerProperty orderCountProperty() {
        return orderCount;
    }

    public ReadOnlyIntegerProperty overdueCountProperty() {
        return overdueCount;
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

        orderCount.set(result.size());
        overdueCount.set(overdue);
    }

    public void openOrder(Order order) {
        if (order != null) host.showOrderDetails(order);
    }
}
