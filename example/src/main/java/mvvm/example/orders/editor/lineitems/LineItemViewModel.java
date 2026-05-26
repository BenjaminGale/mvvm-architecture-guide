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
import java.util.UUID;

public class LineItemViewModel {

    private final Action editAction;
    private final Action deleteAction;

    private UUID productId;
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();
    private final ReadOnlyIntegerWrapper quantity = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BigDecimal> unitPrice = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BigDecimal> total = new ReadOnlyObjectWrapper<>();

    public LineItemViewModel(LineItem lineItem, LineItemHost host) {
        this.productId = lineItem.productId();
        description.set(lineItem.description());
        quantity.set(lineItem.quantity());
        unitPrice.set(lineItem.unitPrice());
        total.set(lineItem.total());

        editAction = new Action(() ->
            host.editLineItem(new LineItemEditorRequest(toLineItem(), host.currentLineItems(), this::onEdited))
        );
        deleteAction = new Action(() -> host.deleteLineItem(this));
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
