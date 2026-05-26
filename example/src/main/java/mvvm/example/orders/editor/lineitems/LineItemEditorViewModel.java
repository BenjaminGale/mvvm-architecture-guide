package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public class LineItemEditorViewModel {

    private final Action selectProductAction;
    private final Action confirmAction;
    private final LineItemEditorRequest request;
    private UUID productId;
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ReadOnlyObjectWrapper<BigDecimal> unitPrice = new ReadOnlyObjectWrapper<>();

    public LineItemEditorViewModel(LineItemEditorRequest request, LineItemEditorHost host) {
        this.request = request;
        this.productId = request.item().productId();
        description.set(request.item().description());
        quantity.set(request.item().quantity());
        unitPrice.set(request.item().unitPrice());

        selectProductAction = new Action(() -> host.selectProduct(
            new ProductSelectorRequest(request.currentLineItems(), productId, this::onProductSelected)
        ));

        confirmAction = new Action(this::confirm, description.isNotEmpty());
    }

    private void onProductSelected(Product product) {
        productId = product.id();
        description.set(product.name());
        unitPrice.set(product.unitPrice());
    }

    public void confirm() {
        request.confirmChanges(new LineItem(productId, description.get(), quantity.get(), unitPrice.get()));
    }

    public Action selectProductAction() { return selectProductAction; }
    public Action confirmAction() { return confirmAction; }

    public ReadOnlyStringProperty descriptionProperty() { return description.getReadOnlyProperty(); }
    public IntegerProperty quantityProperty() { return quantity; }
    public ReadOnlyObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice.getReadOnlyProperty(); }
}
