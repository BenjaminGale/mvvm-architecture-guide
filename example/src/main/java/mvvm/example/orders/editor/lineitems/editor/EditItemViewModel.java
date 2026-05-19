package mvvm.example.orders.editor.lineitems.editor;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.requests.EditItemRequest;
import mvvm.example.orders.requests.SelectProductRequest;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EditItemViewModel {

    public final Action selectProduct;

    private final EditItemRequest request;
    private String productId;
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public EditItemViewModel(EditItemRequest request, Consumer<SelectProductRequest> selectProductHost) {
        this.request = request;
        this.productId = request.getItem().productId();

        description.set(request.getItem().description());
        quantity.set(request.getItem().quantity());
        unitPrice.set(request.getItem().unitPrice());

        selectProduct = new Action(() -> selectProductHost.accept(
            new SelectProductRequest(request.getExcludedProductIds(), this::onProductSelected)
        ));
    }

    private void onProductSelected(Product product) {
        productId = product.id();
        description.set(product.name());
        unitPrice.set(product.unitPrice());
    }

    public void confirm() {
        request.confirmChanges(new LineItem(
            productId,
            description.get(),
            quantity.get(),
            request.getItem().quantityAllocated(),
            unitPrice.get()
        ));
    }

    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
}
