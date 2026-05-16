package mvvm.example.orders.context;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

public class OrderContext {

    private final ReadOnlyIntegerWrapper overdueOrderCount = new ReadOnlyIntegerWrapper(this, "pendingCount", 0);

    public void setCount(int count) {
        overdueOrderCount.set(count);
    }

    public ReadOnlyIntegerProperty overdueOrderCountProperty() {
        return overdueOrderCount.getReadOnlyProperty();
    }
}
