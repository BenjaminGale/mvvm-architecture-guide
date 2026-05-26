package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LineItemViewModel {

    private final Action editAction;
    private final Action deleteAction;

    private UUID productId;
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();
    private final ReadOnlyIntegerWrapper quantity = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BigDecimal> unitPrice = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BigDecimal> total = new ReadOnlyObjectWrapper<>();

    public LineItemViewModel(
        LineItem lineItem,
        Consumer<LineItemEditorRequest> editHost,
        Supplier<List<LineItem>> currentLineItemsSupplier,
        Consumer<LineItemViewModel> deleteCallback
    ) {
        this.productId = lineItem.productId();
        description.set(lineItem.description());
        quantity.set(lineItem.quantity());
        unitPrice.set(lineItem.unitPrice());
        total.set(lineItem.total());

        editAction = new Action(() ->
            editHost.accept(new LineItemEditorRequest(toLineItem(), currentLineItemsSupplier.get(), this::onEdited))
        );
        deleteAction = new Action(() -> deleteCallback.accept(this));
    }

    private void onEdited(LineItem updated) {
        productId = updated.productId();
        description.set(updated.description());
        quantity.set(updated.quantity());
        unitPrice.set(updated.unitPrice());
        total.set(updated.total());
    }

    public LineItem toLineItem() {
        return new LineItem(productId, description.get(), quantity.get(), unitPrice.get());
    }

    public Action editAction() { return editAction; }
    public Action deleteAction() { return deleteAction; }

    public UUID productId() { return productId; }
    public ReadOnlyStringProperty descriptionProperty() { return description.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty quantityProperty() { return quantity.getReadOnlyProperty(); }
    public ReadOnlyObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice.getReadOnlyProperty(); }
    public ReadOnlyObjectProperty<BigDecimal> totalProperty() { return total.getReadOnlyProperty(); }
}
