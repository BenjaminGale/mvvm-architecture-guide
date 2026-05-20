package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.editor.EditOrderRequest;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LineItemsExplorerViewModel extends ExplorerViewModel<LineItemSummary> {

    private final ObservableList<LineItem> lineItems = FXCollections.observableArrayList();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    private final LineItemsService service;
    private final LineItemsHost host;
    private final String orderId;

    public LineItemsExplorerViewModel(EditOrderRequest request, LineItemsService service, LineItemsHost host) {
        this.service = service;
        this.host = host;
        this.orderId = request.orderId();

        lineItems.setAll(service.fetchLineItems(request));
        lineItems.addListener((ListChangeListener<LineItem>) c -> validate());
        validate();
        refreshItems();
    }

    private void validate() {
        valid.set(!lineItems.isEmpty() && lineItems.stream().allMatch(i -> i.productId() != null));
    }

    private void refreshItems() {
        service.fetchSummaries(List.copyOf(lineItems), orderId)
            .thenAccept(results -> items().setAll(results));
    }

    @Override
    protected CompletableFuture<List<LineItemSummary>> fetchItemsAsync() {
        return service.fetchSummaries(List.copyOf(lineItems), orderId);
    }

    @Override
    protected void addItem() {
        Set<String> excluded = lineItems.stream()
            .map(LineItem::productId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        host.showItemEditor(new EditItemRequest(LineItem.empty(), excluded, this::addConfirmedRow));
    }

    @Override
    protected void editItem(LineItemSummary summary) {
        int index = items().indexOf(summary);
        if (index < 0) return;
        var item = lineItems.get(index);
        Set<String> excluded = lineItems.stream()
            .map(LineItem::productId)
            .filter(Objects::nonNull)
            .filter(id -> !id.equals(item.productId()))
            .collect(Collectors.toSet());
        host.showItemEditor(new EditItemRequest(item, excluded, updated -> updateConfirmedRow(index, updated)));
    }

    @Override
    protected void deleteItem(LineItemSummary summary) {
        int index = items().indexOf(summary);
        if (index < 0) return;
        var item = lineItems.get(index);
        lineItems.remove(index);
        service.deleteLineItem(item.productId(), orderId);
        refreshItems();
        selectedItemProperty().set(null);
    }

    public void addConfirmedRow(LineItem item) {
        lineItems.add(item);
        refreshItems();
    }

    public void updateConfirmedRow(int index, LineItem updated) {
        lineItems.set(index, updated);
        refreshItems();
        if (index < items().size()) selectedItemProperty().set(items().get(index));
    }

    public ReadOnlyBooleanProperty validProperty() { return valid; }

    public List<LineItem> buildLineItems() { return List.copyOf(lineItems); }
}
