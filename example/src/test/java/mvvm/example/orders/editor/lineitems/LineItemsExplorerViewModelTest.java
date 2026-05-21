package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.queries.LineItemSummary;
import mvvm.example.orders.editor.EditOrderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.LineItemsViewModel")
class LineItemsExplorerViewModelTest {

    private static final EditOrderRequest AN_ORDER = EditOrderRequest.of("ord-1");

    private static LineItemsExplorerService serviceWith(LineItem... items) {
        var service = mock(LineItemsExplorerService.class);
        when(service.fetchLineItems(any())).thenReturn(List.of(items));
        when(service.fetchSummaries(any(), any())).thenAnswer(inv -> {
            List<LineItem> lineItems = inv.getArgument(0);
            return CompletableFuture.completedFuture(
                lineItems.stream()
                    .map(i -> new LineItemSummary(i.productId(), i.description(), i.quantity(), i.unitPrice(), 0))
                    .toList()
            );
        });
        return service;
    }

    private static LineItem item(String productId) {
        return new LineItem(productId, productId, 1, BigDecimal.TEN);
    }

    private static LineItemsExplorerViewModel withItems(LineItem... items) {
        return new LineItemsExplorerViewModel(AN_ORDER, serviceWith(items), req -> {});
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("the rows are populated from the provided line items")
        void rowsPopulatedFromItems() {
            var vm = withItems(item("a"), item("b"));

            assertEquals(2, vm.items().size());
        }

        @Test
        @DisplayName("the list is valid when all items have a product")
        void validWhenAllItemsHaveProduct() {
            var vm = withItems(item("a"));

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
            var vm = withItems(item("a"));
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.deleteItemAction().execute();

            assertFalse(vm.validProperty().get());
        }

        @Test
        @DisplayName("the list is invalid when any item has no product")
        void invalidWhenAnyItemHasNoProduct() {
            var vm = withItems(item("a"), new LineItem(null, "", 1, BigDecimal.TEN));

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when a row is selected")
    class WhenARowIsSelected {

        @Test
        @DisplayName("canDelete is false when no row is selected")
        void canDeleteIsFalseWithNoSelection() {
            var vm = withItems(item("a"));

            assertFalse(vm.deleteItemAction().canExecute());
        }

        @Test
        @DisplayName("canDelete is true when a row is selected")
        void canDeleteIsTrueWithSelection() {
            var vm = withItems(item("a"));

            vm.selectedItemProperty().set(vm.items().getFirst());

            assertTrue(vm.deleteItemAction().canExecute());
        }

        @Test
        @DisplayName("canDelete becomes false again when the selection is cleared")
        void canDeleteReturnsFalseWhenSelectionCleared() {
            var vm = withItems(item("a"));
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.selectedItemProperty().set(null);

            assertFalse(vm.deleteItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when a row is added")
    class WhenARowIsAdded {

        @Test
        @DisplayName("addItemAction asks the host to show the item editor")
        void addRowInvokesHost() {
            LineItemsExplorerHost host = mock(LineItemsExplorerHost.class);
            var vm = new LineItemsExplorerViewModel(AN_ORDER, serviceWith(item("a")), host);

            vm.addItemAction().execute();

            verify(host).showItemEditor(any());
        }

        @Test
        @DisplayName("addConfirmedRow appends the new row to the list")
        void confirmedRowAppendedToList() {
            var vm = withItems(item("a"));

            vm.addConfirmedRow(item("b"));

            assertEquals(2, vm.items().size());
        }

        @Test
        @DisplayName("addConfirmedRow with no product makes the list invalid")
        void confirmedRowWithNoProductMakesListInvalid() {
            var vm = withItems(item("a"));

            vm.addConfirmedRow(new LineItem(null, "", 1, BigDecimal.TEN));

            assertFalse(vm.validProperty().get());
        }
    }

    @Nested
    @DisplayName("when a row is removed")
    class WhenARowIsRemoved {

        @Test
        @DisplayName("the selected row is removed from the list")
        void selectedRowIsRemoved() {
            var vm = withItems(item("a"), item("b"));
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.deleteItemAction().execute();

            assertEquals(1, vm.items().size());
        }

        @Test
        @DisplayName("canDelete is false when no row is selected")
        void canDeleteFalseWithNoSelection() {
            var vm = withItems(item("a"));

            assertFalse(vm.deleteItemAction().canExecute());
        }

        @Test
        @DisplayName("the service is asked to delete the line item")
        void serviceAskedToDeleteLineItem() {
            var service = serviceWith(item("a"));
            var vm = new LineItemsExplorerViewModel(AN_ORDER, service, req -> {});
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.deleteItemAction().execute();

            verify(service).deleteLineItem("a", "ord-1");
        }
    }

    @Nested
    @DisplayName("when a row is edited")
    class WhenARowIsEdited {

        @Test
        @DisplayName("editItemAction asks the host to show the item editor")
        void editRowInvokesHost() {
            LineItemsExplorerHost host = mock(LineItemsExplorerHost.class);
            var vm = new LineItemsExplorerViewModel(AN_ORDER, serviceWith(item("a")), host);
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.editItemAction().execute();

            verify(host).showItemEditor(any());
        }

        @Test
        @DisplayName("canEdit is false when no row is selected")
        void canEditFalseWithNoSelection() {
            var vm = withItems(item("a"));

            assertFalse(vm.editItemAction().canExecute());
        }

        @Test
        @DisplayName("updateConfirmedRow replaces the item at the given index")
        void updateConfirmedRowReplacesItem() {
            var vm = withItems(item("a"));

            vm.updateConfirmedRow(0, item("b"));

            assertEquals("b", vm.buildLineItems().getFirst().productId());
        }

        @Test
        @DisplayName("updateConfirmedRow keeps the updated item selected")
        void updateConfirmedRowKeepsSelection() {
            var vm = withItems(item("a"));
            vm.selectedItemProperty().set(vm.items().getFirst());

            vm.updateConfirmedRow(0, item("a"));

            assertNotNull(vm.selectedItemProperty().get());
            assertEquals("a", vm.selectedItemProperty().get().productId());
        }
    }

    @Nested
    @DisplayName("when the line items are built")
    class WhenLineItemsAreBuilt {

        @Test
        @DisplayName("the returned items match the current rows")
        void itemsMatchCurrentRows() {
            var vm = withItems(item("a"));

            var items = vm.buildLineItems();

            assertEquals(1, items.size());
            assertEquals("a", items.getFirst().productId());
        }
    }
}
