package mvvm.example.orders.editor;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.Spacer;
import mvvm.example.orders.editor.lineitems.LineItemView;
import mvvm.example.orders.editor.lineitems.LineItemViewModel;

public class OrderEditorView extends BorderPane {

    public OrderEditorView(OrderEditorViewModel viewModel, ViewLocator<Region> viewLocator) {
        setTop(toolbar(viewModel));
        setCenter(body(viewModel, viewLocator));
    }

    private static ToolBar toolbar(OrderEditorViewModel viewModel) {
        return new ToolBar(
            Buttons.button("Save", viewModel.saveAction()),
            Spacer.create(),
            Buttons.button("Copy", viewModel.copyAction()),
            Buttons.button("Delete", viewModel.deleteOrderAction())
        );
    }

    private static VBox body(OrderEditorViewModel viewModel, ViewLocator<Region> viewLocator) {
        var headerView = viewLocator.locate(viewModel.header());
        var lineItemsPane = lineItemsPane(viewModel);
        VBox.setVgrow(lineItemsPane, Priority.ALWAYS);
        return new VBox(headerView, new Separator(), lineItemsPane);
    }

    private static BorderPane lineItemsPane(OrderEditorViewModel viewModel) {
        var table = lineItemsTable(viewModel);
        BorderPane.setMargin(table, new Insets(8));
        var pane = new BorderPane();
        pane.setTop(lineItemsToolbar(viewModel));
        pane.setCenter(table);
        return pane;
    }

    private static ToolBar lineItemsToolbar(OrderEditorViewModel viewModel) {
        return new ToolBar(new Label("Line Items"), Spacer.create(), Buttons.button("Add", viewModel.addLineItemAction()));
    }

    private static TableView<LineItemViewModel> lineItemsTable(OrderEditorViewModel viewModel) {
        var table = new TableView<LineItemViewModel>();
        table.setItems(viewModel.lineItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(LineItemView.columns());
        return table;
    }
}
