package mvvm.example.customers.explorer;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.customers.domain.Customer;

import java.util.List;

public class CustomersExplorerView extends ExplorerView<Customer> {

    public CustomersExplorerView(CustomersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
    }

    @Override
    protected List<TableColumn<Customer, ?>> columns() {
        return List.of(nameColumn(), emailColumn(), statusColumn());
    }

    private static TableColumn<Customer, String> nameColumn() {
        var col = new TableColumn<Customer, String>("Name");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));
        return col;
    }

    private static TableColumn<Customer, String> emailColumn() {
        var col = new TableColumn<Customer, String>("Email");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().email()));
        return col;
    }

    private static TableColumn<Customer, String> statusColumn() {
        var col = new TableColumn<Customer, String>("Status");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status().displayName()));
        return col;
    }
}
