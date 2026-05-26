package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.ProductSelectorViewModel")
class ProductSelectorViewModelTest {

    private static final Product WIDGET    = new Product(UUID.randomUUID(), "Widget",    BigDecimal.valueOf(9.99),  10);
    private static final Product GADGET    = new Product(UUID.randomUUID(), "Gadget",    BigDecimal.valueOf(19.99),  5);
    private static final Product DOOHICKEY = new Product(UUID.randomUUID(), "Doohickey", BigDecimal.valueOf(4.99),  20);

    private static final List<Product> ALL_PRODUCTS = List.of(WIDGET, GADGET, DOOHICKEY);

    private static LineItem itemFor(Product product) {
        return new LineItem(product.id(), "", 1, BigDecimal.ZERO);
    }

    private static ProductSelectorViewModel viewModelFor(List<LineItem> currentLineItems) {
        return new ProductSelectorViewModel(new ProductSelectorRequest(currentLineItems, null, p -> {}), ALL_PRODUCTS);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("all products are shown when nothing is excluded")
        void allProductsShown() {
            var vm = viewModelFor(List.of());

            assertEquals(3, vm.products().size());
        }

        @Test
        @DisplayName("excluded products are not shown")
        void excludedProductsNotShown() {
            var vm = viewModelFor(List.of(itemFor(WIDGET), itemFor(GADGET)));

            assertEquals(1, vm.products().size());
            assertEquals(DOOHICKEY, vm.products().getFirst());
        }
    }

    @Nested
    @DisplayName("when searching")
    class WhenSearching {

        @Test
        @DisplayName("the list is filtered to products whose name contains the search text")
        void filtersOnName() {
            var vm = viewModelFor(List.of());

            vm.searchTextProperty().set("widget");

            assertEquals(List.of(WIDGET), vm.products());
        }

        @Test
        @DisplayName("matching is case-insensitive")
        void caseInsensitiveMatch() {
            var vm = viewModelFor(List.of());

            vm.searchTextProperty().set("GADGET");

            assertEquals(List.of(GADGET), vm.products());
        }

        @Test
        @DisplayName("clearing the search restores all products")
        void clearingSearchRestoresAll() {
            var vm = viewModelFor(List.of());
            vm.searchTextProperty().set("widget");

            vm.searchTextProperty().set("");

            assertEquals(3, vm.products().size());
        }
    }

    @Nested
    @DisplayName("when confirmed")
    class WhenConfirmed {

        @Test
        @DisplayName("the request callback is invoked with the selected product")
        void callbackInvokedWithSelection() {
            Consumer<Product> listener = mock();
            var vm = new ProductSelectorViewModel(new ProductSelectorRequest(List.of(), null, listener), ALL_PRODUCTS);
            vm.selectedProductProperty().set(WIDGET);

            vm.confirm();

            verify(listener).accept(WIDGET);
        }

        @Test
        @DisplayName("confirm action cannot execute when no product is selected")
        void confirmActionDisabledWithoutSelection() {
            var vm = new ProductSelectorViewModel(new ProductSelectorRequest(List.of(), null, _ -> {}), ALL_PRODUCTS);

            assertFalse(vm.confirmAction().canExecute());
        }
    }
}
