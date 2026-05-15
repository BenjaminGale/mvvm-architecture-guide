package mvvm.example.orders.editor;

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.controls.Buttons;

public class OrderEditorView extends BorderPane {

    public OrderEditorView(OrderEditorViewModel viewModel, ViewLocator<Region> viewLocator) {
        var saveBtn = new Button("Save");
        var copyBtn = new Button("Copy");
        var deleteBtn = new Button("Delete");

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var toolbar = new ToolBar(saveBtn, spacer, copyBtn, deleteBtn);

        var headerView = viewLocator.locate(viewModel.getHeader());
        var lineItemsView = viewLocator.locate(viewModel.getLineItems());
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
