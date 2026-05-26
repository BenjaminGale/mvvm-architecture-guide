package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.Dialogs;
import mvvm.example.core.view.controls.SelectorView;
import mvvm.example.stock.domain.Product;

public class ProductSelectorDialog {

    public static Dialog<Runnable> dialog(ProductSelectorViewModel viewModel) {
        return Dialogs.create(
            "Select Product",
            new SelectorView<>(
                "Search products...",
                viewModel.searchTextProperty(),
                viewModel.products(),
                viewModel.selectedProductProperty(),
                Product::name
            ),
            "Select",
            viewModel.confirmAction()
        );
    }

    private ProductSelectorDialog() {}
}
