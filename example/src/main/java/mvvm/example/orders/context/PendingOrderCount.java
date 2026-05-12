package mvvm.example.orders.context;

import javafx.beans.property.ReadOnlyIntegerProperty;

public interface PendingOrderCount {
    ReadOnlyIntegerProperty pendingCountProperty();
}
