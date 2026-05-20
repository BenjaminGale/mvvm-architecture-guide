package mvvm.example.orders.editor;

import mvvm.example.orders.domain.LineItem;

import java.time.LocalDate;
import java.util.List;

public interface OrderEditorService {
    void upsert(String orderId, String customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems);
    String copyOrder(String orderId);
    void deleteOrder(String orderId);
}
