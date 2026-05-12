package mvvm.example.orders;

import javafx.beans.property.ReadOnlyIntegerProperty;

public interface PendingOrderCount {
    ReadOnlyIntegerProperty pendingCountProperty();
}
