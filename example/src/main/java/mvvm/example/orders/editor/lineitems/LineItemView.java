package mvvm.example.orders.editor.lineitems;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import java.util.List;
import javafx.scene.layout.HBox;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.table.CurrencyTableCell;
import mvvm.example.core.view.table.IntegerTableCell;

import java.math.BigDecimal;

public class LineItemView {

    public static List<TableColumn<LineItemViewModel, ?>> columns() {
        return List.of(
            descriptionColumn(),
            quantityColumn(),
            unitPriceColumn(),
            totalColumn(),
            allocatedColumn(),
            actionsColumn()
        );
    }

    private static TableColumn<LineItemViewModel, String> descriptionColumn() {
        var col = new TableColumn<LineItemViewModel, String>("Product");
        col.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        return col;
    }

    private static TableColumn<LineItemViewModel, Number> quantityColumn() {
        var col = new TableColumn<LineItemViewModel, Number>("Qty");
        col.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        col.setCellFactory(_ -> centered(new IntegerTableCell<>()));
        return col;
    }

    private static TableColumn<LineItemViewModel, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItemViewModel, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());
        col.setCellFactory(_ -> centered(new CurrencyTableCell<>()));
        return col;
    }

    private static TableColumn<LineItemViewModel, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItemViewModel, BigDecimal>("Total");
        col.setCellValueFactory(cell -> cell.getValue().totalProperty());
        col.setCellFactory(_ -> centered(new CurrencyTableCell<>()));
        return col;
    }

    private static TableColumn<LineItemViewModel, Number> allocatedColumn() {
        var col = new TableColumn<LineItemViewModel, Number>("Allocated");
        col.setCellValueFactory(cell -> cell.getValue().allocatedQuantityProperty());
        col.setCellFactory(_ -> centered(new IntegerTableCell<>()));
        return col;
    }

    private static TableColumn<LineItemViewModel, Void> actionsColumn() {
        var col = new TableColumn<LineItemViewModel, Void>("");
        col.setCellFactory(_ -> centered(new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(4, editBtn, deleteBtn);
            private LineItemViewModel bound = null;

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                var row = getTableRow();
                var vm = (!empty && row != null) ? row.getItem() : null;
                if (vm != bound) {
                    editBtn.disableProperty().unbind();
                    deleteBtn.disableProperty().unbind();
                    bound = vm;
                    if (vm != null) {
                        Buttons.bind(editBtn, vm.editAction);
                        Buttons.bind(deleteBtn, vm.deleteAction);
                    }
                }
                setGraphic(vm != null ? box : null);
            }
        }));
        return col;
    }

    private static <S, T> TableCell<S, T> centered(TableCell<S, T> cell) {
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private LineItemView() {}
}
