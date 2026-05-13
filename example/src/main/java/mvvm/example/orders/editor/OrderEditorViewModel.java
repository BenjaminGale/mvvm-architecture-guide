package mvvm.example.orders.editor;

import javafx.beans.binding.Bindings;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemRowViewModel;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;

import java.util.concurrent.CompletableFuture;

public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final Order order;
    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;
    private final OrderEditorHost host;

    public OrderEditorViewModel(
        Order order,
        OrderService orderService,
        OrderEditorHost host
    ) {
        this.order = order;
        this.host = host;
        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems(), this::editRow);

        this.save = new AsyncAction(() ->
           CompletableFuture.supplyAsync(() -> {
                orderService.save(buildUpdatedOrder());
                return host::returnToList;
           }), Bindings.and(header.validProperty(), lineItems.validProperty()));

        this.delete = new Action(() -> {
            orderService.delete(order.id());
            host.returnToList();
        });

        this.copy = new Action(() -> {
            var copied = orderService.copy(order.id());
            host.openOrder(copied);
        });
    }

    private void editRow(LineItemRowViewModel row) {
        host.showItemEditor(new EditItemSession(row.toLineItem(), updated -> {
            row.descriptionProperty().set(updated.description());
            row.quantityProperty().set(updated.quantity());
            row.unitPriceProperty().set(updated.unitPrice());
        }));
    }

    public OrderHeaderViewModel getHeader()  { return header; }
    public LineItemsViewModel getLineItems() { return lineItems; }

    public Order buildUpdatedOrder() {
        return order
            .withHeader(header.buildHeader())
            .withLineItems(lineItems.buildLineItems());
    }
}
