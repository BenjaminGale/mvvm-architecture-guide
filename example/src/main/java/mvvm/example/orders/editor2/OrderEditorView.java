package mvvm.example.orders.editor2;

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
import mvvm.example.orders.editor2.lineitems.LineItemView;
import mvvm.example.orders.editor2.lineitems.LineItemViewModel;

public class OrderEditorView extends BorderPane {

    public OrderEditorView(OrderEditorViewModel viewModel, ViewLocator<Region> viewLocator) {
        var saveBtn = new Button("Save");
        var copyBtn = new Button("Copy");
        var deleteOrderBtn = new Button("Delete");
        Buttons.bind(saveBtn, viewModel.saveAction);
        Buttons.bind(copyBtn, viewModel.copyAction);
        Buttons.bind(deleteOrderBtn, viewModel.deleteOrderAction);
        var toolbar = new ToolBar(saveBtn, Spacer.create(), copyBtn, deleteOrderBtn);

        var headerView = viewLocator.locate(viewModel.header());

        var addBtn = new Button("Add");
        Buttons.bind(addBtn, viewModel.addLineItemAction);
        var lineItemsToolbar = new ToolBar(new Label("Line Items"), Spacer.create(), addBtn);

        var table = new TableView<LineItemViewModel>();
        table.setItems(viewModel.lineItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
            LineItemView.descriptionColumn(),
            LineItemView.quantityColumn(),
            LineItemView.unitPriceColumn(),
            LineItemView.totalColumn(),
            LineItemView.allocatedColumn(),
            LineItemView.actionsColumn()
        );

        var lineItemsPane = new BorderPane();
        lineItemsPane.setTop(lineItemsToolbar);
        lineItemsPane.setCenter(table);
        BorderPane.setMargin(table, new Insets(8, 8, 8, 8));

        VBox.setVgrow(lineItemsPane, Priority.ALWAYS);
        var center = new VBox(headerView, new Separator(), lineItemsPane);

        setTop(toolbar);
        setCenter(center);
    }
}
