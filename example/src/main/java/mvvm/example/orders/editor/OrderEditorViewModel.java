package mvvm.example.orders.editor;

import javafx.beans.binding.Bindings;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.editor.header.OrderHeaderService;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsExplorerViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsHost;
import mvvm.example.orders.editor.lineitems.LineItemsService;

import java.util.concurrent.CompletableFuture;

public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final Order order;

    private final OrderHeaderViewModel header;
    private final LineItemsExplorerViewModel lineItems;

    private final OrderEditorService service;
    private final OrderEditorHost host;

    public OrderEditorViewModel(
        EditOrderRequest request,
        OrderHeaderService headerService,
        LineItemsService lineItemsService,
        LineItemsHost lineItemsHost,
        OrderEditorService service,
        OrderEditorHost host
    ) {
        this.service = service;
        this.host = host;

        // TODO: This shouldn't be in the constructor...
        this.order = request.isNew()
            ? Order.empty()
            : service.fetchOrder(request.orderId());

        this.header = new OrderHeaderViewModel(request, headerService, host::showCustomerSelector);
        this.lineItems = new LineItemsExplorerViewModel(request, lineItemsService, lineItemsHost);

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
        host.openOrder(EditOrderRequest.of(copiedId));
    }

    public OrderHeaderViewModel getHeader()  { return header; }
    public LineItemsExplorerViewModel getLineItems() { return lineItems; }

    public Order buildUpdatedOrder() {
        var customer = header.selectedCustomerProperty().get();
        var plannedShipDate = header.plannedShipDateProperty().get();
        var reference = header.referenceProperty().get();
        var items = lineItems.buildLineItems();
        return new Order(order.id(), customer.id(), order.createdDate(), plannedShipDate, reference, order.status(), order.completionDate(), items);
    }
}
