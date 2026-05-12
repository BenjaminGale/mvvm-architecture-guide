package mvvm.example.orders;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

public class OrderContext implements PendingOrderCount, PendingOrderCounter {

    private final ReadOnlyIntegerWrapper pendingCount = new ReadOnlyIntegerWrapper(this, "pendingCount", 0);

    @Override
    public void setPendingCount(int count) {
        pendingCount.set(count);
    }

    @Override
    public ReadOnlyIntegerProperty pendingCountProperty() {
        return pendingCount.getReadOnlyProperty();
    }
}
