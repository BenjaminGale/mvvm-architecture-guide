package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.LineItemsViewModel")
class LineItemsViewModelTest {

    private static LineItem namedItem(String description) {
        return new LineItem(null, description, 1, BigDecimal.TEN);
    }

    private static LineItemsViewModel withItems(LineItem... items) {
        return new LineItemsViewModel(List.of(items), (i, item) -> {}, () -> {});
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
        @DisplayName("addRow invokes the add callback")
        void addRowInvokesCallback() {
            Runnable onAddRow = mock();
            var vm = new LineItemsViewModel(List.of(namedItem("Widget")), (i, item) -> {}, onAddRow);

            vm.addRow();

            verify(onAddRow).run();
        }

        @Test
        @DisplayName("addConfirmedRow appends the new row to the list")
        void confirmedRowAppendedToList() {
            var vm = withItems(namedItem("Widget"));

            vm.addConfirmedRow(namedItem("Gadget"));

            assertEquals(2, vm.getRows().size());
        }

        @Test
        @DisplayName("addConfirmedRow with a blank description makes the list invalid")
        void confirmedRowWithBlankDescriptionMakesListInvalid() {
            var vm = withItems(namedItem("Widget"));

            vm.addConfirmedRow(namedItem(""));

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
        @DisplayName("the edit callback is invoked with the index and item")
        void editCallbackInvokedWithIndexAndItem() {
            BiConsumer<Integer, LineItem> onEditRow = mock();
            var vm = new LineItemsViewModel(List.of(namedItem("Widget")), onEditRow, () -> {});
            vm.selectRow(vm.getRows().getFirst());

            vm.editSelected();

            verify(onEditRow).accept(eq(0), any(LineItem.class));
        }

        @Test
        @DisplayName("the edit callback is not invoked when no row is selected")
        void editCallbackNotInvokedWithNoSelection() {
            BiConsumer<Integer, LineItem> onEditRow = mock();
            var vm = new LineItemsViewModel(List.of(namedItem("Widget")), onEditRow, () -> {});

            vm.editSelected();

            verify(onEditRow, never()).accept(any(), any());
        }

        @Test
        @DisplayName("updateConfirmedRow replaces the item at the given index")
        void updateConfirmedRowReplacesItem() {
            var vm = withItems(namedItem("Widget"));

            vm.updateConfirmedRow(0, namedItem("Updated Widget"));

            assertEquals("Updated Widget", vm.getRows().getFirst().description());
        }

        @Test
        @DisplayName("updateConfirmedRow keeps the updated item selected")
        void updateConfirmedRowKeepsSelection() {
            var vm = withItems(namedItem("Widget"));
            vm.selectRow(vm.getRows().getFirst());

            var updated = namedItem("Updated Widget");
            vm.updateConfirmedRow(0, updated);

            assertEquals(updated, vm.selectedRowProperty().get());
        }
    }

    @Nested
    @DisplayName("when the line items are built")
    class WhenLineItemsAreBuilt {

        @Test
        @DisplayName("the returned items match the current rows")
        void itemsMatchCurrentRows() {
            var vm = withItems(namedItem("Widget"));

            var items = vm.buildLineItems();

            assertEquals(1, items.size());
            assertEquals("Widget", items.getFirst().description());
        }
    }
}
