package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductSelectorViewModel {

    private final ProductSelectorRequest request;
    private final StringProperty searchText = new SimpleStringProperty("");
    private final ObjectProperty<Product> selectedProduct = new SimpleObjectProperty<>();
    private final FilteredList<Product> products;

    public ProductSelectorViewModel(ProductSelectorRequest request, List<Product> allProducts) {
        this.request = request;

        Set<String> excluded = request.currentLineItems()
            .stream()
            .map(LineItem::productId)
            .filter(id -> id != null && !id.equals(request.currentProductId()))
            .collect(Collectors.toSet());

        var available = FXCollections.observableArrayList(
            allProducts.stream().filter(p -> !excluded.contains(p.id())).toList()
        );

        products = new FilteredList<>(available);

        searchText.addListener((obs, old, val) ->
            products.setPredicate(p ->
                val == null || val.isBlank() || p.name().toLowerCase().contains(val.toLowerCase())
            )
        );
    }

    public void confirm() {
        if (selectedProduct.get() != null) request.confirmSelection(selectedProduct.get());
    }

    public StringProperty searchTextProperty() { return searchText; }
    public ObjectProperty<Product> selectedProductProperty() { return selectedProduct; }
    public FilteredList<Product> getProducts() { return products; }
}
