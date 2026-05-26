package mvvm.example.orders.editor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.editor.header.CustomerSelectorRequest;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemEditorRequest;
import mvvm.example.orders.editor.lineitems.LineItemViewModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.isEmpty;

public class OrderEditorViewModel {

    public final AsyncAction saveAction;
    public final Action copyAction;
    public final Action deleteOrderAction;
    public final Action addLineItemAction;

    private final OrderEditorRequest request;
    private final OrderEditorService service;
    private final OrderEditorHost host;
    private final OrderHeaderViewModel header;
    private final ObservableList<LineItemViewModel> lineItems = FXCollections.observableArrayList();
    private final Consumer<LineItemEditorRequest> editLineItemHost;

    public OrderEditorViewModel(
        OrderEditorRequest request,
        OrderEditorService service,
        OrderEditorHost host,
        Consumer<CustomerSelectorRequest> selectCustomerHost,
        Consumer<LineItemEditorRequest> editLineItemHost
    ) {
        this.request = request;
        this.service = service;
        this.host = host;
        this.editLineItemHost = editLineItemHost;

        var data = service.fetch(request);
        this.header = new OrderHeaderViewModel(data.order(), data.customer(), selectCustomerHost);

        data.order().lineItems().forEach(item -> lineItems.add(createLineItemVm(item)));

        this.saveAction = new AsyncAction(this::onSave, header.validProperty().and(isEmpty(lineItems).not()));
        this.copyAction = new Action(this::onCopy);
        this.deleteOrderAction = new Action(this::onDeleteOrder);
        this.addLineItemAction = new Action(this::onAddLineItem);
    }

    private LineItemViewModel createLineItemVm(LineItem item) {
        return new LineItemViewModel(
            item,
            editLineItemHost,
            () -> lineItems.stream().map(LineItemViewModel::toLineItem).toList(),
            lineItems::remove
        );
    }

    private void onAddLineItem() {
        var currentItems = lineItems.stream().map(LineItemViewModel::toLineItem).toList();
        editLineItemHost.accept(new LineItemEditorRequest(LineItem.draft(), currentItems, item ->
            lineItems.add(createLineItemVm(item))
        ));
    }

    private CompletableFuture<Runnable> onSave() {
        return CompletableFuture.supplyAsync(() -> {
            service.save(
                request.orderId(),
                header.selectedCustomerProperty().get().id(),
                header.referenceProperty().get(),
                header.plannedShipDateProperty().get(),
                lineItems.stream().map(LineItemViewModel::toLineItem).toList()
            );
            return host::returnToList;
        });
    }

    private void onCopy() {
        host.openOrder(OrderEditorRequest.of(service.copy(request.orderId())));
    }

    private void onDeleteOrder() {
        service.delete(request.orderId());
        host.returnToList();
    }

    public OrderHeaderViewModel header() { return header; }
    public ObservableList<LineItemViewModel> lineItems() { return lineItems; }
}
