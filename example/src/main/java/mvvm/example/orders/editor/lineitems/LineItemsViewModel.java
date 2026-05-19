package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.LineItemSummary;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class LineItemsViewModel extends ExplorerViewModel<LineItemSummary> {

    private final ObservableList<LineItem> lineItems = FXCollections.observableArrayList();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    private final Function<List<LineItem>, CompletableFuture<List<LineItemSummary>>> fetchSummaries;
    private final Runnable onAdd;
    private final BiConsumer<Integer, LineItem> onEdit;
    private final Consumer<LineItem> onDelete;

    public LineItemsViewModel(
        List<LineItem> items,
        Function<List<LineItem>, CompletableFuture<List<LineItemSummary>>> fetchSummaries,
        Runnable onAdd,
        BiConsumer<Integer, LineItem> onEdit,
        Consumer<LineItem> onDelete
    ) {
        this.fetchSummaries = fetchSummaries;
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        lineItems.setAll(items);
        lineItems.addListener((ListChangeListener<LineItem>) c -> validate());
        validate();
        refreshItems();
    }

    private void validate() {
        valid.set(!lineItems.isEmpty() && lineItems.stream().allMatch(i -> i.productId() != null));
    }

    private void refreshItems() {
        fetchSummaries.apply(List.copyOf(lineItems))
            .thenAccept(results -> items().setAll(results));
    }

    @Override
    protected CompletableFuture<List<LineItemSummary>> fetchItemsAsync() {
        return fetchSummaries.apply(List.copyOf(lineItems));
    }

    @Override
    protected void addItem() {
        onAdd.run();
    }

    @Override
    protected void editItem(LineItemSummary summary) {
        int index = items().indexOf(summary);
        if (index < 0) return;
        onEdit.accept(index, lineItems.get(index));
    }

    @Override
    protected void deleteItem(LineItemSummary summary) {
        int index = items().indexOf(summary);
        if (index < 0) return;
        var item = lineItems.get(index);
        lineItems.remove(index);
        onDelete.accept(item);
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
