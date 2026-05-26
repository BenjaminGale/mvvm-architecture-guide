package mvvm.example.orders.editor.header;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.Dialogs;
import mvvm.example.core.view.controls.SelectorView;
import mvvm.example.customers.domain.Customer;

public class CustomerSelectorDialog {

    public static Dialog<Runnable> dialog(CustomerSelectorViewModel viewModel) {
        return Dialogs.create(
            "Select Customer",
            new SelectorView<>(
                "Search customers...",
                viewModel.searchTextProperty(),
                viewModel.customers(),
                viewModel.selectedCustomerProperty(),
                Customer::name
            ),
            "Select",
            viewModel.confirmAction()
        );
    }

    private CustomerSelectorDialog() {}
}
