package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.LineItemViewModel")
class LineItemViewModelTest {

    private static final UUID PROD_1 = UUID.randomUUID();
    private static final UUID PROD_2 = UUID.randomUUID();
    private static final LineItem WIDGET = new LineItem(PROD_1, "Widget", 2, BigDecimal.valueOf(9.99));

    private static LineItemHost noOpHost() {
        return new LineItemHost() {
            public void editLineItem(LineItemEditorRequest req) {}
            public List<LineItem> currentLineItems() { return List.of(); }
            public void deleteLineItem(LineItemViewModel vm) {}
        };
    }

    private static LineItemViewModel viewModelFor(LineItem item) {
        return new LineItemViewModel(item, noOpHost());
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("description is populated from the line item")
        void descriptionPopulated() {
            assertEquals("Widget", viewModelFor(WIDGET).descriptionProperty().get());
        }

        @Test
        @DisplayName("quantity is populated from the line item")
        void quantityPopulated() {
            assertEquals(2, viewModelFor(WIDGET).quantityProperty().get());
        }

        @Test
        @DisplayName("unit price is populated from the line item")
        void unitPricePopulated() {
            assertEquals(BigDecimal.valueOf(9.99), viewModelFor(WIDGET).unitPriceProperty().get());
        }

        @Test
        @DisplayName("total is populated from the line item")
        void totalPopulated() {
            assertEquals(WIDGET.total(), viewModelFor(WIDGET).totalProperty().get());
        }
    }

    @Nested
    @DisplayName("when edit is triggered")
    class WhenEditTriggered {

        @Test
        @DisplayName("the edit host receives the current item")
        void editHostReceivesCurrentItem() {
            var captured = new LineItemEditorRequest[]{null};
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { captured[0] = req; }
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals(WIDGET, captured[0].item());
        }

        @Test
        @DisplayName("the edit host receives the current line items")
        void editHostReceivesCurrentLineItems() {
            var other = new LineItem(PROD_2, "Gadget", 1, BigDecimal.TEN);
            var captured = new LineItemEditorRequest[]{null};
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { captured[0] = req; }
                public List<LineItem> currentLineItems() { return List.of(other); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals(List.of(other), captured[0].currentLineItems());
        }

        @Test
        @DisplayName("description updates when the edit is confirmed")
        void descriptionUpdatesOnConfirm() {
            var updated = new LineItem(PROD_1, "Super Widget", 2, BigDecimal.valueOf(9.99));
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { req.confirmChanges(updated); }
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals("Super Widget", vm.descriptionProperty().get());
        }

        @Test
        @DisplayName("quantity updates when the edit is confirmed")
        void quantityUpdatesOnConfirm() {
            var updated = new LineItem(PROD_1, "Widget", 5, BigDecimal.valueOf(9.99));
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { req.confirmChanges(updated); }
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals(5, vm.quantityProperty().get());
        }

        @Test
        @DisplayName("unit price updates when the edit is confirmed")
        void unitPriceUpdatesOnConfirm() {
            var updated = new LineItem(PROD_1, "Widget", 2, BigDecimal.valueOf(19.99));
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { req.confirmChanges(updated); }
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals(BigDecimal.valueOf(19.99), vm.unitPriceProperty().get());
        }

        @Test
        @DisplayName("total updates when the edit is confirmed")
        void totalUpdatesOnConfirm() {
            var updated = new LineItem(PROD_1, "Widget", 5, BigDecimal.valueOf(9.99));
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) { req.confirmChanges(updated); }
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel vm) {}
            });

            vm.editAction().execute();

            assertEquals(updated.total(), vm.totalProperty().get());
        }
    }

    @Nested
    @DisplayName("when delete is triggered")
    class WhenDeleteTriggered {

        @Test
        @DisplayName("the delete host is invoked with this view model")
        void deleteHostInvokedWithSelf() {
            var deleted = new LineItemViewModel[]{null};
            var vm = new LineItemViewModel(WIDGET, new LineItemHost() {
                public void editLineItem(LineItemEditorRequest req) {}
                public List<LineItem> currentLineItems() { return List.of(); }
                public void deleteLineItem(LineItemViewModel v) { deleted[0] = v; }
            });

            vm.deleteAction().execute();

            assertSame(vm, deleted[0]);
        }
    }
}
