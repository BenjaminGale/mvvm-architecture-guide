package mvvm.example.orders.editor.edititem;

import mvvm.example.orders.domain.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EditItemViewModel")
class EditItemViewModelTest {

    private static final LineItem ORIGINAL = new LineItem("Widget", 2, BigDecimal.valueOf(9.99));

    private static EditItemViewModel viewModelFor(LineItem item) {
        return new EditItemViewModel(new EditItemSession(item, confirmed -> {}));
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the description property is populated from the session item")
        void descriptionPopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals("Widget", vm.descriptionProperty().get());
        }

        @Test
        @DisplayName("the quantity property is populated from the session item")
        void quantityPopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals(2, vm.quantityProperty().get());
        }

        @Test
        @DisplayName("the unit price property is populated from the session item")
        void unitPricePopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals(BigDecimal.valueOf(9.99), vm.unitPriceProperty().get());
        }
    }

    @Nested
    @DisplayName("when the edit is confirmed")
    class WhenConfirmed {

        @Test
        @DisplayName("the session callback is invoked with the updated item")
        void sessionCallbackInvokedWithUpdatedItem() {
            var confirmed = new AtomicReference<LineItem>();
            var session = new EditItemSession(ORIGINAL, confirmed::set);
            var vm = new EditItemViewModel(session);

            vm.confirm();

            assertNotNull(confirmed.get());
        }

        @Test
        @DisplayName("the confirmed item reflects the edited property values")
        void confirmedItemReflectsEditedValues() {
            var confirmed = new AtomicReference<LineItem>();
            var session = new EditItemSession(ORIGINAL, confirmed::set);
            var vm = new EditItemViewModel(session);
            vm.descriptionProperty().set("Gadget");
            vm.quantityProperty().set(5);
            vm.unitPriceProperty().set(BigDecimal.valueOf(19.99));

            vm.confirm();

            assertEquals("Gadget", confirmed.get().description());
            assertEquals(5, confirmed.get().quantity());
            assertEquals(BigDecimal.valueOf(19.99), confirmed.get().unitPrice());
        }
    }
}
