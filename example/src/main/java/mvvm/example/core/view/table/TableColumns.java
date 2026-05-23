package mvvm.example.core.view.table;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

public class TableColumns {

    public static <S, T> TableColumn<S, T> column(String title, Function<S, T> valueFn) {
        var col = new TableColumn<S, T>(title);
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(valueFn.apply(cell.getValue())));
        return col;
    }

    public static <S, T> TableColumn<S, T> column(String title, Function<S, T> valueFn,
            Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory) {
        var col = column(title, valueFn);
        col.setCellFactory(cellFactory);
        return col;
    }

    private TableColumns() {}
}
