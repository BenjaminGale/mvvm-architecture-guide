package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductSelectorViewModel {

    private final ProductSelectorRequest request;
    private final StringProperty searchText = new SimpleStringProperty("");
    private final ObjectProperty<Product> selectedProduct = new SimpleObjectProperty<>();
    private final FilteredList<Product> products;
    private final Action confirmAction;

    public ProductSelectorViewModel(ProductSelectorRequest request, List<Product> allProducts) {
        this.request = request;

        Set<UUID> excluded = request.currentLineItems()
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

        confirmAction = new Action(this::confirm, selectedProduct.isNotNull());
    }

    public void confirm() {
        request.confirmSelection(selectedProduct.get());
    }

    public Action confirmAction() { return confirmAction; }
    public StringProperty searchTextProperty() { return searchText; }
    public ObjectProperty<Product> selectedProductProperty() { return selectedProduct; }
    public FilteredList<Product> getProducts() { return products; }
}
