package mvvm.example.orders.explorer;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.context.PendingOrderCounter;

import java.util.Comparator;
import java.util.function.Consumer;

public class OrdersExplorerViewModel {

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final StringProperty statusText = new SimpleStringProperty("");

    private final OrderService orderService;
    private final PendingOrderCounter orderContext;
    private final Consumer<Order> onOrderSelected;

    public OrdersExplorerViewModel(
        OrderService orderService,
        PendingOrderCounter orderContext,
        Consumer<Order> onOrderSelected
    ) {
        this.orderService = orderService;
        this.orderContext = orderContext;
        this.onOrderSelected = onOrderSelected;
        refresh();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyStringProperty statusTextProperty() {
        return statusText;
    }

    public void refresh() {
        var result = orderService.fetchAll()
            .stream()
            .sorted(Comparator.comparing(Order::date).reversed())
            .toList();

        orders.setAll(result);

        var pendingCount = (int) result.stream().filter(Order::isOverdue).count();
        orderContext.setPendingCount(pendingCount);

        statusText.set(result.size() + " orders, " + pendingCount + " overdue");
    }

    public void openOrder(Order order) {
        if (order != null) onOrderSelected.accept(order);
    }
}
