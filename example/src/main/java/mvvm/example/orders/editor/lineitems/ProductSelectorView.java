package mvvm.example.orders.editor.lineitems;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.stock.domain.Product;

public class ProductSelectorView extends VBox {

    public static Dialog<Runnable> dialog(ProductSelectorViewModel viewModel) {
        var selectBtn = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<Runnable>();
        dialog.setTitle("Select Product");
        dialog.getDialogPane().setContent(new ProductSelectorView(viewModel));
        dialog.getDialogPane().setPrefWidth(380);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == selectBtn ? viewModel::confirm : null);

        var okButton = (Button) dialog.getDialogPane().lookupButton(selectBtn);
        okButton.disableProperty().bind(viewModel.selectedProductProperty().isNull());

        return dialog;
    }

    private ProductSelectorView(ProductSelectorViewModel viewModel) {
        setSpacing(8);
        setPadding(new Insets(8));
        setPrefHeight(280);

        var searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        var list = new ListView<Product>();
        list.setItems(viewModel.getProducts());
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        list.getSelectionModel().selectedItemProperty().addListener((obs, old, product) -> {
            if (product != null) viewModel.selectedProductProperty().set(product);
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        getChildren().addAll(searchField, list);

        Controls.focusOnShow(searchField);
    }
}
