package mvvm.example.orders.editor.header;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.controls.SelectorView;
import mvvm.example.customers.domain.Customer;

public class CustomerSelectorView {

    public static Dialog<Runnable> dialog(CustomerSelectorViewModel viewModel) {
        return SelectorView.dialog(
            "Select Customer",
            "Search customers...",
            viewModel.searchTextProperty(),
            viewModel.getCustomers(),
            viewModel.selectedCustomerProperty(),
            Customer::name,
            viewModel::confirm
        );
    }

    private CustomerSelectorView() {}
}
