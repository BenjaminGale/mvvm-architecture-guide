package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.controls.SelectorView;
import mvvm.example.stock.domain.Product;

public class ProductSelectorView {

    public static Dialog<Runnable> dialog(ProductSelectorViewModel viewModel) {
        return SelectorView.dialog(
            "Select Product",
            "Search products...",
            viewModel.searchTextProperty(),
            viewModel.getProducts(),
            viewModel.selectedProductProperty(),
            Product::name,
            viewModel::confirm
        );
    }

    private ProductSelectorView() {}
}
