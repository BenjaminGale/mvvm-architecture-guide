package mvvm.example.orders.explorer;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.orders.domain.Order;

import java.util.Comparator;

public class OrdersExplorerViewModel {

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final StringProperty statusText = new SimpleStringProperty("");

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host) {
        this.service = service;
        this.host = host;
        refresh();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyStringProperty statusTextProperty() {
        return statusText;
    }

    public void refresh() {
        var result = service
            .fetchAllOrders()
            .stream()
            .sorted(Comparator.comparing(Order::date).reversed())
            .toList();

        orders.setAll(result);

        var pendingCount = (int) result.stream().filter(Order::isOverdue).count();
        host.setPendingOrderCount(pendingCount);

        statusText.set(result.size() + " orders, " + pendingCount + " overdue");
    }

    public void openOrder(Order order) {
        if (order != null) host.showOrderDetails(order);
    }
}
