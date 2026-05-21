package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class LineItemEditorViewModel {

    public final Action selectProduct;

    private final LineItemEditorRequest request;
    private String productId;
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public LineItemEditorViewModel(LineItemEditorRequest request, Consumer<ProductSelectorRequest> selectProductHost) {
        this.request = request;
        this.productId = request.item().productId();

        description.set(request.item().description());
        quantity.set(request.item().quantity());
        unitPrice.set(request.item().unitPrice());

        selectProduct = new Action(() -> selectProductHost.accept(
            new ProductSelectorRequest(request.excludedProductIds(), this::onProductSelected)
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
            unitPrice.get()
        ));
    }

    public ReadOnlyStringProperty descriptionProperty() { return description; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ReadOnlyObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
}
