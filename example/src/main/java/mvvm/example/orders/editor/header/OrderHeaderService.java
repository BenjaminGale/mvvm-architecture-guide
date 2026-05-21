package mvvm.example.orders.editor.header;

import mvvm.example.orders.domain.queries.OrderHeaderSummary;
import mvvm.example.orders.editor.OrderEditorRequest;

public interface OrderHeaderService {
    OrderHeaderSummary fetch(OrderEditorRequest request);
}
