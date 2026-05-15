package mvvm.example.orders.editor;

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.orders.editor.header.OrderHeaderView;
import mvvm.example.orders.editor.lineitems.LineItemsView;

public class OrderEditorView extends BorderPane {

    public OrderEditorView(OrderEditorViewModel viewModel) {
        var saveBtn = new Button("Save");
        var copyBtn = new Button("Copy");
        var deleteBtn = new Button("Delete");

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var toolbar = new ToolBar(saveBtn, spacer, copyBtn, deleteBtn);

        var headerView = new OrderHeaderView(viewModel.getHeader());
        var lineItemsView = new LineItemsView(viewModel.getLineItems());
        VBox.setVgrow(lineItemsView, Priority.ALWAYS);

        var center = new VBox(
            headerView,
            new Separator(),
            lineItemsView
        );

        setTop(toolbar);
        setCenter(center);

        Buttons.bind(saveBtn, viewModel.save);
        Buttons.bind(copyBtn, viewModel.copy);
        Buttons.bind(deleteBtn, viewModel.delete);
    }
}
