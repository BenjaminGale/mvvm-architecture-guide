package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import mvvm.example.orders.requests.SelectProductRequest;
import mvvm.example.stock.domain.Product;

import java.util.List;

public class ProductSelectorViewModel {

    private final StringProperty searchText = new SimpleStringProperty(this, "searchText", "");
    private final ObjectProperty<Product> selectedProduct = new SimpleObjectProperty<>(this, "selectedProduct");
    private final FilteredList<Product> products;
    private final SelectProductRequest request;

    public ProductSelectorViewModel(SelectProductRequest request, List<Product> allProducts) {
        this.request = request;
        var available = allProducts.stream()
            .filter(p -> !request.getExcludedProductIds().contains(p.id()))
            .toList();
        this.products = new FilteredList<>(FXCollections.observableArrayList(available));

        searchText.addListener((_, _, text) ->
            products.setPredicate(product ->
                text == null ||
                text.isBlank() ||
                product.name().toLowerCase().contains(text.toLowerCase())
            )
        );
    }

    public void confirm() {
        if (selectedProduct.get() != null)
            request.confirmSelection(selectedProduct.get());
    }

    public StringProperty searchTextProperty() { return searchText; }
    public ObjectProperty<Product> selectedProductProperty() { return selectedProduct; }
    public FilteredList<Product> getProducts() { return products; }
}
