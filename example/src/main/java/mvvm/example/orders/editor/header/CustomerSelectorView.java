package mvvm.example.orders.editor.header;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.customers.domain.Customer;

public class CustomerSelectorView extends VBox {

    public static Dialog<Runnable> dialog(CustomerSelectorViewModel viewModel) {
        var selectBtn = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<Runnable>();
        dialog.setTitle("Select Customer");
        dialog.getDialogPane().setContent(new CustomerSelectorView(viewModel));
        dialog.getDialogPane().setPrefWidth(380);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == selectBtn ? viewModel::confirm : null);

        var okButton = (Button) dialog.getDialogPane().lookupButton(selectBtn);
        okButton.disableProperty().bind(viewModel.selectedCustomerProperty().isNull());

        return dialog;
    }

    private CustomerSelectorView(CustomerSelectorViewModel viewModel) {
        setSpacing(8);
        setPadding(new Insets(8));
        setPrefHeight(280);

        var searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        var list = new ListView<Customer>();
        list.setItems(viewModel.getCustomers());
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        list.getSelectionModel().selectedItemProperty().addListener((obs, old, customer) -> {
            if (customer != null) viewModel.selectedCustomerProperty().set(customer);
        });

        var current = viewModel.selectedCustomerProperty().get();
        if (current != null) {
            list.getSelectionModel().select(current);
            list.scrollTo(current);
        }

        VBox.setVgrow(list, Priority.ALWAYS);
        getChildren().addAll(searchField, list);

        Controls.focusOnShow(searchField);
    }
}
