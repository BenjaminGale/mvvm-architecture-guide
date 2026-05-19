package mvvm.example.orders.editor.lineitems.editor;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.requests.EditItemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Consumer;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.EditItemViewModel")
class EditItemViewModelTest {

    private static final LineItem ORIGINAL = new LineItem("Widget", 2, BigDecimal.valueOf(9.99));

    private static EditItemViewModel viewModelFor(LineItem item) {
        return new EditItemViewModel(new EditItemRequest(item, Set.of(), confirmed -> {}), r -> {});
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
            Consumer<LineItem> listener = mock();
            var vm = new EditItemViewModel(new EditItemRequest(ORIGINAL, Set.of(), listener), r -> {});

            vm.confirm();

            verify(listener).accept(any(LineItem.class));
        }

        @Test
        @DisplayName("the confirmed item reflects the edited property values")
        void confirmedItemReflectsEditedValues() {
            Consumer<LineItem> listener = mock();
            var vm = new EditItemViewModel(new EditItemRequest(ORIGINAL, Set.of(), listener), r -> {});
            vm.descriptionProperty().set("Gadget");
            vm.quantityProperty().set(5);
            vm.unitPriceProperty().set(BigDecimal.valueOf(19.99));

            vm.confirm();

            var captor = ArgumentCaptor.forClass(LineItem.class);
            verify(listener).accept(captor.capture());
            assertEquals("Gadget", captor.getValue().description());
            assertEquals(5, captor.getValue().quantity());
            assertEquals(BigDecimal.valueOf(19.99), captor.getValue().unitPrice());
        }
    }
}
