package mvvm.example.orders.editor;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.editor.OrderEditorRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderEditorService {
    OrderEditorData fetch(OrderEditorRequest request);
    UUID save(UUID orderId, UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems);
    UUID copy(UUID orderId);
    void delete(UUID orderId);
}
