package mvvm.example.customers.explorer;

import javafx.scene.control.TableColumn;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.table.TableColumns;
import mvvm.example.core.view.table.TableViews;
import mvvm.example.customers.domain.Customer;

import java.util.List;

public class CustomersExplorerView extends ExplorerView<Customer> {

    public CustomersExplorerView(CustomersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
    }

    @Override
    protected List<TableColumn<Customer, ?>> columns() {
        return List.of(
            TableColumns.column("Name", Customer::name),
            TableColumns.column("Email", Customer::email),
            TableColumns.column("Status", c -> c.status().displayName())
        );
    }
}
