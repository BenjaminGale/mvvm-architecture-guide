package mvvm.example.orders.editor;

import mvvm.example.orders.editor.header.CustomerSelectorRequest;
import mvvm.example.orders.editor.lineitems.LineItemEditorRequest;

public interface OrderEditorHost {
    void returnToList();
    void openOrder(OrderEditorRequest request);
    void selectCustomer(CustomerSelectorRequest request);
    void editLineItem(LineItemEditorRequest request);
}
