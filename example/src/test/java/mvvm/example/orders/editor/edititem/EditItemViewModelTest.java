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
        return new EditItemViewModel(new EditItemRequest(item, confirmed -> {}));
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the description property is populated from the request item")
        void descriptionPopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals("Widget", vm.descriptionProperty().get());
        }

        @Test
        @DisplayName("the quantity property is populated from the request item")
        void quantityPopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals(2, vm.quantityProperty().get());
        }

        @Test
        @DisplayName("the unit price property is populated from the request item")
        void unitPricePopulated() {
            var vm = viewModelFor(ORIGINAL);

            assertEquals(BigDecimal.valueOf(9.99), vm.unitPriceProperty().get());
        }
    }

    @Nested
    @DisplayName("when the edit is confirmed")
    class WhenConfirmed {

        @Test
        @DisplayName("the request callback is invoked with the updated item")
        void requestCallbackInvokedWithUpdatedItem() {
            var confirmed = new AtomicReference<LineItem>();
            var request = new EditItemRequest(ORIGINAL, confirmed::set);
            var vm = new EditItemViewModel(request);

            vm.confirm();

            assertNotNull(confirmed.get());
        }

        @Test
        @DisplayName("the confirmed item reflects the edited property values")
        void confirmedItemReflectsEditedValues() {
            var confirmed = new AtomicReference<LineItem>();
            var request = new EditItemRequest(ORIGINAL, confirmed::set);
            var vm = new EditItemViewModel(request);
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
