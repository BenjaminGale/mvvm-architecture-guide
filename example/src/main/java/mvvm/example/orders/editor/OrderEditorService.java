package mvvm.example.orders.editor;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.editor.OrderEditorRequest;

import java.time.LocalDate;
import java.util.List;

public interface OrderEditorService {
    OrderEditorData fetch(OrderEditorRequest request);
    String save(String orderId, String customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems);
    String copy(String orderId);
    void delete(String orderId);
}
