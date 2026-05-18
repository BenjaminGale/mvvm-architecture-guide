package mvvm.example.core.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.viewmodel.ExplorerViewModel;

import java.util.List;

public abstract class ExplorerView<T> extends BorderPane {

    private final TableView<T> table = new TableView<>();

    protected ExplorerView(ExplorerViewModel<T> viewModel) {
        table.setItems(viewModel.items());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(columns());

        viewModel.selectedItemProperty().bind(table.getSelectionModel().selectedItemProperty());

        var addButton = new Button("Add");
        addButton.disableProperty().bind(viewModel.addItemAction().canExecuteProperty().not());
        addButton.setOnAction(_ -> viewModel.addItemAction().execute());

        BorderPane.setMargin(table, new Insets(8));
        setTop(new ToolBar(addButton));
        setCenter(table);

        Controls.focusOnShow(table);
        viewModel.fetchItemsAction().executeAsync(Platform::runLater);
    }

    protected TableView<T> table() {
        return table;
    }

    protected abstract List<TableColumn<T, ?>> columns();
}
