package mvvm.example.shell.main.sidebar;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import mvvm.example.orders.context.PendingOrderCount;

public class SidebarViewModel {

    private final IntegerProperty pendingOrderCount = new SimpleIntegerProperty();
    private final SidebarHost host;

    public SidebarViewModel(PendingOrderCount orderContext, SidebarHost host) {
        pendingOrderCount.bind(orderContext.pendingCountProperty());
        this.host = host;
    }

    public ReadOnlyIntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }

    public void openOrdersWorkspace() { host.openOrdersWorkspace(); }
    public void openCustomersWorkspace() { host.openCustomersWorkspace(); }
}
