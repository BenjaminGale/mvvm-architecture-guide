package mvvm.example.orders.editor.header;

import mvvm.example.orders.domain.queries.OrderHeaderSummary;
import mvvm.example.orders.editor.EditOrderRequest;

public interface OrderHeaderService {
    OrderHeaderSummary fetch(EditOrderRequest request);
}
