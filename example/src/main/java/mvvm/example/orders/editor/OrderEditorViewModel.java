package mvvm.example.orders.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import mvvm.example.orders.editor.edititem.EditItemSession;
import mvvm.example.orders.editor.header.OrderHeaderViewModel;
import mvvm.example.orders.editor.lineitems.LineItemRow;
import mvvm.example.orders.editor.lineitems.LineItemsViewModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final Order order;
    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;
    private final BooleanProperty canSave = new SimpleBooleanProperty(false);
    private final Consumer<EditItemSession> onEditItem;

    public OrderEditorViewModel(
        Order order,
        OrderService orderService,
        OrderEditorRequests requests,
        Consumer<EditItemSession> onEditItem
    ) {
        this.order = order;
        this.onEditItem = onEditItem;
        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems(), this::editRow);

        canSave.bind(header.validProperty().and(lineItems.validProperty()));

        this.save = new AsyncAction(() ->
           CompletableFuture.supplyAsync(() -> {
                orderService.save(buildUpdatedOrder());
                return requests.onSaved();
           }), canSave);

        this.delete = new Action(() -> {
            orderService.delete(order.id());
            requests.onDeleted().run();
        });

        this.copy = new Action(() -> {
            var copied = orderService.copy(order.id());
            requests.onCopied().accept(copied);
        });
    }

    private void editRow(LineItemRow row) {
        onEditItem.accept(new EditItemSession(row.toLineItem(), updated -> {
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
