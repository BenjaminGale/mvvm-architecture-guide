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
import mvvm.example.orders.LineItem;

import java.util.List;
import java.util.function.Consumer;

public class LineItemsViewModel {

    private final ObservableList<LineItemRow> rows = FXCollections.observableArrayList();
    private final ObjectProperty<LineItemRow> selectedRow = new SimpleObjectProperty<>();
    private final BooleanProperty canRemove = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    private final Consumer<LineItemRow> onEditRow;

    public LineItemsViewModel(List<LineItem> items, Consumer<LineItemRow> onEditRow) {
        this.onEditRow = onEditRow;
        rows.setAll(items.stream().map(LineItemRow::new).toList());

        selectedRow.addListener(obs -> canRemove.set(selectedRow.get() != null));
        rows.addListener((ListChangeListener<LineItemRow>) c -> validate());

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
        rows.add(new LineItemRow(LineItem.empty()));
    }

    public void removeSelected() {
        var row = selectedRow.get();
        if (row != null) rows.remove(row);
    }

    public void editSelected() {
        var row = selectedRow.get();
        if (row != null) onEditRow.accept(row);
    }

    public void selectRow(LineItemRow row) {
        selectedRow.set(row);
    }

    public ObservableList<LineItemRow> getRows()                      { return rows; }
    public ReadOnlyBooleanProperty canRemoveProperty()                { return canRemove; }
    public ReadOnlyBooleanProperty validProperty()                    { return valid; }
    public ReadOnlyObjectProperty<LineItemRow> selectedRowProperty()  { return selectedRow; }

    public List<LineItem> buildLineItems() {
        return rows.stream().map(LineItemRow::toLineItem).toList();
    }
}
