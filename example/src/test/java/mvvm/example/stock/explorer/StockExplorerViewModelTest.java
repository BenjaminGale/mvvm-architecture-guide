package mvvm.example.stock.explorer;

import mvvm.example.core.viewmodel.ExplorerViewModelTest;
import mvvm.example.stock.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Stock.StockExplorerViewModel")
class StockExplorerViewModelTest extends ExplorerViewModelTest<Product, StockExplorerViewModel> {

    private static Product product(String id, String name) {
        return new Product(id, name, BigDecimal.ONE, 10);
    }

    @Override
    protected StockExplorerViewModel createViewModel() {
        return new StockExplorerViewModel(List::of);
    }

    @Override
    protected Product createItem() {
        return product("1", "Widget");
    }

    @Nested
    @DisplayName("add action")
    class AddAction {

        @Test
        @DisplayName("is disabled until implemented")
        void disabledUntilImplemented() {
            assertFalse(createViewModel().addItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("edit action")
    class EditAction {

        @Test
        @DisplayName("is disabled until implemented")
        void disabledUntilImplemented() {
            var vm = createViewModel();
            vm.selectedItemProperty().set(createItem());
            assertFalse(vm.editItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("delete action")
    class DeleteAction {

        @Test
        @DisplayName("is disabled until implemented")
        void disabledUntilImplemented() {
            var vm = createViewModel();
            vm.selectedItemProperty().set(createItem());
            assertFalse(vm.deleteItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("products are loaded and sorted alphabetically by name")
        void productsSortedAlphabetically() {
            var vm = new StockExplorerViewModel(
                () -> List.of(product("1", "Zebra Part"), product("2", "Acme Widget"))
            );
            executeFetch(vm);

            assertEquals("Acme Widget", vm.items().getFirst().name());
            assertEquals("Zebra Part", vm.items().getLast().name());
        }
    }
}
