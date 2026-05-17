package mvvm.example.orders.editor;

import javafx.beans.binding.Bindings;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemRowViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;
import mvvm.example.orders.requests.EditOrderRequest;

import java.util.concurrent.CompletableFuture;

public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final Order order;

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;

    private final OrderEditorService service;
    private final OrderEditorHost host;

    public OrderEditorViewModel(
        EditOrderRequest request,
        OrderEditorService service,
        OrderEditorHost host
    ) {
        this.service = service;
        this.host = host;

        // TODO: This shouldn't be in the constructor...
        this.order = service.fetchOrder(request.orderId());

        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems(), this::editRow);

        this.save = new AsyncAction(this::onSave, Bindings.and(header.validProperty(), lineItems.validProperty()));
        this.delete = new Action(this::onDelete);
        this.copy = new Action(this::onCopy);
    }

    private CompletableFuture<Runnable> onSave() {
        return CompletableFuture.supplyAsync(() -> {
            service.saveOrder(buildUpdatedOrder());
            return host::returnToList;
        });
    }

    private void onDelete() {
        service.deleteOrder(order.id());
        host.returnToList();
    }

    private void onCopy() {
        var copiedId = service.copyOrder(order.id());
        host.openOrder(new EditOrderRequest(copiedId));
    }

    private void editRow(LineItemRowViewModel row) {
        host.showItemEditor(
            new EditItemRequest(
                row.toLineItem(),
                updated -> saveEditedRow(row, updated)
            )
        );
    }

    private void saveEditedRow(LineItemRowViewModel row, LineItem updated) {
        row.descriptionProperty().set(updated.description());
        row.quantityProperty().set(updated.quantity());
        row.unitPriceProperty().set(updated.unitPrice());
    }

    public OrderHeaderViewModel getHeader()  { return header; }
    public LineItemsViewModel getLineItems() { return lineItems; }

    public Order buildUpdatedOrder() {
        return order
            .withHeader(header.buildHeader())
            .withLineItems(lineItems.buildLineItems());
    }
}
