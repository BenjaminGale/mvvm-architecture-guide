package mvvm.example.customers.explorer;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import mvvm.example.customers.Customer;

public class CustomersExplorerView extends StackPane {

    public CustomersExplorerView(CustomersViewModel viewModel) {
        var table = new TableView<Customer>();
        table.setItems(viewModel.getCustomers());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(nameColumn());
        table.getColumns().add(emailColumn());

        getChildren().add(table);

        table.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, selected) -> viewModel.openCustomer(selected));
    }

    private TableColumn<Customer, String> nameColumn() {
        var col = new TableColumn<Customer, String>("Name");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));
        return col;
    }

    private TableColumn<Customer, String> emailColumn() {
        var col = new TableColumn<Customer, String>("Email");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().email()));
        return col;
    }
}
