package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.editor.lineitems.SelectProductRequest;
import mvvm.example.stock.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Orders.ProductSelectorViewModel")
class ProductSelectorViewModelTest {

    private static final Product WIDGET = new Product("prod-1", "Widget", BigDecimal.valueOf(9.99), 10);
    private static final Product GADGET = new Product("prod-2", "Gadget", BigDecimal.valueOf(19.99), 5);
    private static final Product DOOHICKEY = new Product("prod-3", "Doohickey", BigDecimal.valueOf(4.99), 20);

    private static final List<Product> ALL_PRODUCTS = List.of(WIDGET, GADGET, DOOHICKEY);

    private static ProductSelectorViewModel viewModelFor(Set<String> excluded) {
        return new ProductSelectorViewModel(new SelectProductRequest(excluded, p -> {}), ALL_PRODUCTS);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("all products are shown when nothing is excluded")
        void allProductsShown() {
            var vm = viewModelFor(Set.of());

            assertEquals(3, vm.getProducts().size());
        }

        @Test
        @DisplayName("excluded products are not shown")
        void excludedProductsNotShown() {
            var vm = viewModelFor(Set.of("prod-1", "prod-2"));

            assertEquals(1, vm.getProducts().size());
            assertEquals(DOOHICKEY, vm.getProducts().getFirst());
        }
    }

    @Nested
    @DisplayName("when searching")
    class WhenSearching {

        @Test
        @DisplayName("the list is filtered to products whose name contains the search text")
        void filtersOnName() {
            var vm = viewModelFor(Set.of());

            vm.searchTextProperty().set("widget");

            assertEquals(List.of(WIDGET), vm.getProducts());
        }

        @Test
        @DisplayName("matching is case-insensitive")
        void caseInsensitiveMatch() {
            var vm = viewModelFor(Set.of());

            vm.searchTextProperty().set("GADGET");

            assertEquals(List.of(GADGET), vm.getProducts());
        }

        @Test
        @DisplayName("clearing the search restores all products")
        void clearingSearchRestoresAll() {
            var vm = viewModelFor(Set.of());
            vm.searchTextProperty().set("widget");

            vm.searchTextProperty().set("");

            assertEquals(3, vm.getProducts().size());
        }
    }

    @Nested
    @DisplayName("when confirmed")
    class WhenConfirmed {

        @Test
        @DisplayName("the request callback is invoked with the selected product")
        void callbackInvokedWithSelection() {
            Consumer<Product> listener = mock();
            var vm = new ProductSelectorViewModel(new SelectProductRequest(Set.of(), listener), ALL_PRODUCTS);
            vm.selectedProductProperty().set(WIDGET);

            vm.confirm();

            verify(listener).accept(WIDGET);
        }

        @Test
        @DisplayName("the callback is not invoked when no product is selected")
        void callbackNotInvokedWithoutSelection() {
            Consumer<Product> listener = mock();
            var vm = new ProductSelectorViewModel(new SelectProductRequest(Set.of(), listener), ALL_PRODUCTS);

            vm.confirm();

            verifyNoInteractions(listener);
        }
    }
}
