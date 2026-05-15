package mvvm.example.shell.sidebar;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import mvvm.example.orders.context.PendingOrderCount;

public class SidebarViewModel {

    private final IntegerProperty pendingOrderCount = new SimpleIntegerProperty();
    private final Runnable onOrders;
    private final Runnable onCustomers;

    public SidebarViewModel(
        PendingOrderCount orderContext,
        Runnable onOrders,
        Runnable onCustomers
    ) {
        pendingOrderCount.bind(orderContext.pendingCountProperty());
        this.onOrders = onOrders;
        this.onCustomers = onCustomers;
    }

    public ReadOnlyIntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }

    public void navigateToOrders() { onOrders.run(); }
    public void navigateToCustomers() { onCustomers.run(); }
}
