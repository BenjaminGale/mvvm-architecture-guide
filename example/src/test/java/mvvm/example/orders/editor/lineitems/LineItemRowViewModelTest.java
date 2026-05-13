package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LineItemRowViewModel")
class LineItemRowViewModelTest {

    private static LineItemRowViewModel row(int quantity, BigDecimal unitPrice) {
        return new LineItemRowViewModel(new LineItem("Item", quantity, unitPrice));
    }

    @Nested
    @DisplayName("total")
    class Total {

        @Test
        @DisplayName("is quantity multiplied by unit price")
        void isQuantityMultipliedByUnitPrice() {
            var row = row(3, new BigDecimal("4.50"));

            assertEquals(new BigDecimal("13.50"), row.totalProperty().get());
        }

        @Test
        @DisplayName("is zero when unit price is null")
        void isZeroWhenUnitPriceIsNull() {
            var row = new LineItemRowViewModel(new LineItem("Item", 2, null));

            assertEquals(BigDecimal.ZERO, row.totalProperty().get());
        }

        @Test
        @DisplayName("updates when quantity changes")
        void updatesWhenQuantityChanges() {
            var row = row(2, new BigDecimal("5.00"));

            row.quantityProperty().set(4);

            assertEquals(new BigDecimal("20.00"), row.totalProperty().get());
        }

        @Test
        @DisplayName("updates when unit price changes")
        void updatesWhenUnitPriceChanges() {
            var row = row(3, new BigDecimal("2.00"));

            row.unitPriceProperty().set(new BigDecimal("3.00"));

            assertEquals(new BigDecimal("9.00"), row.totalProperty().get());
        }
    }
}
