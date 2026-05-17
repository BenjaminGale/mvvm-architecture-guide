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
import java.util.function.Consumer;

public class LineItemsViewModel {

    private final ObservableList<LineItemRowViewModel> rows = FXCollections.observableArrayList();
    private final ObjectProperty<LineItemRowViewModel> selectedRow = new SimpleObjectProperty<>();
    private final BooleanProperty canRemove = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    private final Consumer<LineItemRowViewModel> onEditRow;
    private final Runnable onAddRow;

    public LineItemsViewModel(List<LineItem> items, Consumer<LineItemRowViewModel> onEditRow, Runnable onAddRow) {
        this.onEditRow = onEditRow;
        this.onAddRow = onAddRow;
        rows.setAll(items.stream().map(LineItemRowViewModel::new).toList());

        selectedRow.addListener(obs -> canRemove.set(selectedRow.get() != null));
        rows.addListener((ListChangeListener<LineItemRowViewModel>) c -> validate());

        validate();
    }

    private void validate() {
        valid.set(
            !rows.isEmpty() &&
            rows.stream().allMatch(r -> {
                var desc = r.descriptionProperty().get();
                return desc != null && !desc.isBlank();
            })
        );
    }

    public void addRow() {
        onAddRow.run();
    }

    public void addConfirmedRow(LineItem item) {
        rows.add(new LineItemRowViewModel(item));
    }

    public void removeSelected() {
        var row = selectedRow.get();
        if (row != null) rows.remove(row);
    }

    public void editSelected() {
        var row = selectedRow.get();
        if (row != null) onEditRow.accept(row);
    }

    public void selectRow(LineItemRowViewModel row) {
        selectedRow.set(row);
    }

    public ObservableList<LineItemRowViewModel> getRows() { return rows; }
    public ReadOnlyBooleanProperty canRemoveProperty() { return canRemove; }
    public ReadOnlyBooleanProperty validProperty() { return valid; }
    public ReadOnlyObjectProperty<LineItemRowViewModel> selectedRowProperty() { return selectedRow; }

    public List<LineItem> buildLineItems() {
        return rows.stream().map(LineItemRowViewModel::toLineItem).toList();
    }
}
