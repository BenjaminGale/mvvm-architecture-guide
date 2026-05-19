package mvvm.example.orders.editor;

import javafx.beans.binding.Bindings;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.FulfilledOrder;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.PendingOrder;
import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;
import mvvm.example.orders.requests.EditOrderRequest;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        this.order = request.isNew()
            ? Order.empty()
            : service.fetchOrder(request.orderId());

        var currentCustomer = order.customerId() != null
            ? service.findCustomer(order.customerId()).orElse(null)
            : null;

        this.header = new OrderHeaderViewModel(order, currentCustomer, host::showCustomerSelector);
        this.lineItems = new LineItemsViewModel(order.lineItems(), items -> service.fetchLineItemSummaries(items, order.id()), this::addRow, this::editRow, item -> {});

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

    private void addRow() {
        Set<String> excluded = lineItems.buildLineItems().stream()
            .map(LineItem::productId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        host.showItemEditor(new EditItemRequest(LineItem.empty(), excluded, lineItems::addConfirmedRow));
    }

    private void editRow(int index, LineItem item) {
        Set<String> excluded = lineItems.buildLineItems().stream()
            .map(LineItem::productId)
            .filter(Objects::nonNull)
            .filter(id -> !id.equals(item.productId()))
            .collect(Collectors.toSet());

        host.showItemEditor(new EditItemRequest(item, excluded, updated -> lineItems.updateConfirmedRow(index, updated)));
    }

    public OrderHeaderViewModel getHeader()  { return header; }
    public LineItemsViewModel getLineItems() { return lineItems; }

    public Order buildUpdatedOrder() {
        var customer = header.selectedCustomerProperty().get();
        var plannedShipDate = header.plannedShipDateProperty().get();
        var reference = header.referenceProperty().get();
        var items = lineItems.buildLineItems();
        return switch (order) {
            case PendingOrder p -> new PendingOrder(p.id(), customer.id(), p.createdDate(), plannedShipDate, reference, items);
            case FulfilledOrder f -> new FulfilledOrder(f.id(), customer.id(), f.createdDate(), plannedShipDate, reference, items);
            default -> throw new IllegalStateException("Cannot update a " + order.status() + " order");
        };
    }
}
