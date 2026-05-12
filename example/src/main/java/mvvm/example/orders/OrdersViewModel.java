package mvvm.example.orders;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.function.Consumer;

public class OrdersViewModel {

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final StringProperty statusText = new SimpleStringProperty("");

    private final LoadOrdersUseCase loadOrders;
    private final PendingOrderCounter orderContext;
    private final Consumer<Order> onOrderSelected;

    public OrdersViewModel(
        LoadOrdersUseCase loadOrders,
        PendingOrderCounter orderContext,
        Consumer<Order> onOrderSelected
    ) {
        this.loadOrders = loadOrders;
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
        var result = loadOrders.execute()
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
