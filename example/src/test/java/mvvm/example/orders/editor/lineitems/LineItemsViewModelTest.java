package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.LineItemsViewModel")
class LineItemsViewModelTest {

    private static LineItem namedItem(String description) {
        return new LineItem(description, 1, BigDecimal.TEN);
    }

    private static LineItemsViewModel withItems(LineItem... items) {
        return new LineItemsViewModel(List.of(items), row -> {});
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the rows are populated from the provided line items")
        void rowsPopulatedFromItems() {
            var vm = withItems(namedItem("Widget"), namedItem("Gadget"));

            assertEquals(2, vm.getRows().size());
        }

        @Test
        @DisplayName("the list is valid when all items have a non-blank description")
        void validWhenAllDescriptionsPopulated() {
            var vm = withItems(namedItem("Widget"));

            assertTrue(vm.validProperty().get());
        }

        @Test
        @DisplayName("the list is invalid when there are no items")
        void invalidWhenEmpty() {
            var vm = withItems();

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when validity changes")
    class WhenValidityChanges {

        @Test
        @DisplayName("the list becomes invalid when all rows are removed")
        void becomesInvalidWhenAllRowsRemoved() {
            var vm = withItems(namedItem("Widget"));
            vm.selectRow(vm.getRows().getFirst());

            vm.removeSelected();

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the list is invalid when any row has a blank description")
        void invalidWhenAnyDescriptionBlank() {
            var vm = withItems(namedItem("Widget"), namedItem(""));

            assertFalse(vm.validProperty().get());
        }

    }

    @Nested
    @DisplayName("when a row is selected")
    class WhenARowIsSelected {

        @Test
        @DisplayName("canRemove is false when no row is selected")
        void canRemoveIsFalseWithNoSelection() {
            var vm = withItems(namedItem("Widget"));

            assertFalse(vm.canRemoveProperty().get());
        }

        @Test
        @DisplayName("canRemove is true when a row is selected")
        void canRemoveIsTrueWithSelection() {
            var vm = withItems(namedItem("Widget"));

            vm.selectRow(vm.getRows().getFirst());

            assertTrue(vm.canRemoveProperty().get());
        }

        @Test
        @DisplayName("canRemove becomes false again when the selection is cleared")
        void canRemoveReturnsFalseWhenSelectionCleared() {
            var vm = withItems(namedItem("Widget"));
            vm.selectRow(vm.getRows().getFirst());

            vm.selectRow(null);

            assertFalse(vm.canRemoveProperty().get());
        }
    }

    @Nested
    @DisplayName("when a row is added")
    class WhenARowIsAdded {

        @Test
        @DisplayName("the new row is appended to the list")
        void rowAppendedToList() {
            var vm = withItems(namedItem("Widget"));

            vm.addRow();

            assertEquals(2, vm.getRows().size());
        }

        @Test
        @DisplayName("the list becomes invalid because the new row has a blank description")
        void listBecomesInvalidAfterAdd() {
            var vm = withItems(namedItem("Widget"));

            vm.addRow();

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when a row is removed")
    class WhenARowIsRemoved {

        @Test
        @DisplayName("the selected row is removed from the list")
        void selectedRowIsRemoved() {
            var vm = withItems(namedItem("Widget"), namedItem("Gadget"));
            vm.selectRow(vm.getRows().getFirst());

            vm.removeSelected();

            assertEquals(1, vm.getRows().size());
        }

        @Test
        @DisplayName("nothing happens when no row is selected")
        void nothingHappensWithNoSelection() {
            var vm = withItems(namedItem("Widget"));

            vm.removeSelected();

            assertEquals(1, vm.getRows().size());
        }
    }

    @Nested
    @DisplayName("when a row is edited")
    class WhenARowIsEdited {

        @Test
        @DisplayName("the edit callback is invoked with the selected row")
        void editCallbackInvokedWithSelectedRow() {
            var edited = new AtomicReference<LineItemRowViewModel>();
            var vm = new LineItemsViewModel(List.of(namedItem("Widget")), edited::set);
            vm.selectRow(vm.getRows().getFirst());

            vm.editSelected();

            assertNotNull(edited.get());
        }

        @Test
        @DisplayName("the edit callback is not invoked when no row is selected")
        void editCallbackNotInvokedWithNoSelection() {
            var edited = new AtomicReference<LineItemRowViewModel>();
            var vm = new LineItemsViewModel(List.of(namedItem("Widget")), edited::set);

            vm.editSelected();

            assertNull(edited.get());
        }
    }

    @Nested
    @DisplayName("when the line items are built")
    class WhenLineItemsAreBuilt {

        @Test
        @DisplayName("the returned items reflect the current row values")
        void itemsReflectCurrentRowValues() {
            var vm = withItems(namedItem("Widget"));
            vm.getRows().getFirst().descriptionProperty().set("Updated Widget");
            vm.getRows().getFirst().quantityProperty().set(3);

            var items = vm.buildLineItems();

            assertEquals(1, items.size());
            assertEquals("Updated Widget", items.getFirst().description());
            assertEquals(3, items.getFirst().quantity());
        }
    }
}
