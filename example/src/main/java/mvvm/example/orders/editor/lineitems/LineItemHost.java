package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;

import java.util.List;

public interface LineItemHost {
    void editLineItem(LineItemEditorRequest request);
    List<LineItem> currentLineItems();
    void deleteLineItem(LineItemViewModel vm);
}
