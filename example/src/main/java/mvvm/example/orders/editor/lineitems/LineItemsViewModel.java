package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mvvm.example.orders.domain.LineItem;

import java.util.List;
import java.util.function.BiConsumer;

public class LineItemsViewModel {

    private final ObservableList<LineItem> rows = FXCollections.observableArrayList();
    private final ObjectProperty<LineItem> selectedRow = new SimpleObjectProperty<>();
    private final BooleanProperty canRemove = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    private final BiConsumer<Integer, LineItem> onEditRow;
    private final Runnable onAddRow;

    public LineItemsViewModel(List<LineItem> items, BiConsumer<Integer, LineItem> onEditRow, Runnable onAddRow) {
        this.onEditRow = onEditRow;
        this.onAddRow = onAddRow;
        rows.setAll(items);

        selectedRow.addListener(obs -> canRemove.set(selectedRow.get() != null));
        rows.addListener((ListChangeListener<LineItem>) c -> validate());

        validate();
    }

    private void validate() {
        valid.set(
            !rows.isEmpty() &&
            rows.stream().allMatch(item -> {
                var desc = item.description();
                return desc != null && !desc.isBlank();
            })
        );
    }

    public void addRow() {
        onAddRow.run();
    }

    public void addConfirmedRow(LineItem item) {
        rows.add(item);
    }

    public void updateConfirmedRow(int index, LineItem updated) {
        rows.set(index, updated);
        selectedRow.set(updated);
    }

    public void removeSelected() {
        var item = selectedRow.get();
        if (item != null) rows.remove(item);
    }

    public void editSelected() {
        var item = selectedRow.get();
        if (item != null) onEditRow.accept(rows.indexOf(item), item);
    }

    public void selectRow(LineItem item) {
        selectedRow.set(item);
    }

    public ObservableList<LineItem> getRows() { return rows; }
    public ReadOnlyBooleanProperty canRemoveProperty() { return canRemove; }
    public ReadOnlyBooleanProperty validProperty() { return valid; }
    public ReadOnlyObjectProperty<LineItem> selectedRowProperty() { return selectedRow; }

    public List<LineItem> buildLineItems() {
        return List.copyOf(rows);
    }
}
