package mvvm.example.customers.explorer;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.TableViews;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;

public class CustomersExplorerView extends BorderPane {

    public CustomersExplorerView(CustomersExplorerViewModel viewModel) {
        var table = new TableView<Customer>();
        table.setItems(viewModel.getCustomers());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(nameColumn());
        table.getColumns().add(emailColumn());
        table.getColumns().add(statusColumn());

        BorderPane.setMargin(table, new Insets(8));
        setCenter(table);

        TableViews.onActivate(table, viewModel::openCustomer);
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

    private TableColumn<Customer, String> statusColumn() {
        var col = new TableColumn<Customer, String>("Status");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status().displayName()));
        return col;
    }
}
