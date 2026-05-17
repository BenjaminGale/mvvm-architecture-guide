package mvvm.example.core.view.controls;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import mvvm.example.core.viewmodel.Action;

import java.util.function.Consumer;

public class TableViews {

    public static <T> void bind(TableView<T> table, Action action) {
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                action.execute();
            }
        });

        table.setRowFactory(_ -> {
            var row = new TableRow<T>();

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    action.execute();
                }
            });

            return row;
        });
    }

    public static <T> void onActivate(TableView<T> table, Consumer<T> action) {
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                action.accept(table.getSelectionModel().getSelectedItem());
        });

        table.setRowFactory(_ -> {
            var row = new TableRow<T>();

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    action.accept(row.getItem());
            });

            return row;
        });
    }

    private TableViews() {}
}
