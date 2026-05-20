package mvvm.example.orders.editor;

import javafx.beans.binding.Bindings;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerViewModel;

import java.util.concurrent.CompletableFuture;

public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final EditOrderRequest request;
    private final OrderHeaderViewModel header;
    private final LineItemsExplorerViewModel lineItems;

    private final OrderEditorService service;
    private final OrderEditorHost host;

    public OrderEditorViewModel(
        EditOrderRequest request,
        OrderHeaderViewModel header,
        LineItemsExplorerViewModel lineItems,
        OrderEditorService service,
        OrderEditorHost host
    ) {
        this.request = request;
        this.header = header;
        this.lineItems = lineItems;
        this.service = service;
        this.host = host;

        this.save = new AsyncAction(this::onSave, Bindings.and(header.validProperty(), lineItems.validProperty()));
        this.delete = new Action(this::onDelete);
        this.copy = new Action(this::onCopy);
    }

    private CompletableFuture<Runnable> onSave() {
        return CompletableFuture.supplyAsync(() -> {
            service.upsert(
                request.orderId(),
                header.selectedCustomerProperty().get().id(),
                header.referenceProperty().get(),
                header.plannedShipDateProperty().get(),
                lineItems.buildLineItems()
            );
            return host::returnToList;
        });
    }

    private void onDelete() {
        service.deleteOrder(request.orderId());
        host.returnToList();
    }

    private void onCopy() {
        var copiedId = service.copyOrder(request.orderId());
        host.openOrder(EditOrderRequest.of(copiedId));
    }

    public OrderHeaderViewModel getHeader() { return header; }
    public LineItemsExplorerViewModel getLineItems() { return lineItems; }
}
