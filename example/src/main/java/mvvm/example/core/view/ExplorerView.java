package mvvm.example.core.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.ExplorerViewModel;

import java.util.List;

public abstract class ExplorerView<T> extends BorderPane {

    private final TableView<T> table = new TableView<>();

    protected ExplorerView(ExplorerViewModel<T> viewModel) {
        setTop(toolbar(viewModel.addItemAction()));
        setCenter(setupTable(viewModel));

        Controls.focusOnShow(table);
        Controls.onAttached(this, () -> viewModel.fetchItemsAction().executeAsync(Platform::runLater));
    }

    protected TableView<T> table() {
        return table;
    }

    protected abstract List<TableColumn<T, ?>> columns();

    private TableView<T> setupTable(ExplorerViewModel<T> viewModel) {
        table.setItems(viewModel.items());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(columns());
        viewModel.selectedItemProperty().bind(table.getSelectionModel().selectedItemProperty());
        BorderPane.setMargin(table, new Insets(8));
        return table;
    }

    private static ToolBar toolbar(Action addAction) {
        return new ToolBar(Buttons.button("Add", addAction));
    }
}
