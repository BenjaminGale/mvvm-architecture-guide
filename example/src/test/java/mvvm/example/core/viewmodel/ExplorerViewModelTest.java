package mvvm.example.core.viewmodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ExplorerViewModelTest<T, VM extends ExplorerViewModel<T>> {

    protected abstract VM createViewModel();

    protected abstract T createItem();

    protected void executeFetch(VM vm) {
        vm.fetchItemsAction().executeAsync(Runnable::run);
    }

    @Nested
    @DisplayName("add action")
    class AddAction {

        @Test
        @DisplayName("is enabled when no item is selected")
        void enabledWithNoSelection() {
            var vm = createViewModel();
            assertTrue(vm.addItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("edit action")
    class EditAction {

        @Test
        @DisplayName("is disabled when no item is selected")
        void disabledWithNoSelection() {
            var vm = createViewModel();
            assertFalse(vm.editItemAction().canExecute());
        }

        @Test
        @DisplayName("is enabled when an item is selected")
        void enabledWithSelection() {
            var vm = createViewModel();
            vm.selectedItemProperty().set(createItem());
            assertTrue(vm.editItemAction().canExecute());
        }
    }

    @Nested
    @DisplayName("delete action")
    class DeleteAction {

        @Test
        @DisplayName("is disabled when no item is selected")
        void disabledWithNoSelection() {
            var vm = createViewModel();
            assertFalse(vm.deleteItemAction().canExecute());
        }

        @Test
        @DisplayName("is enabled when an item is selected")
        void enabledWithSelection() {
            var vm = createViewModel();
            vm.selectedItemProperty().set(createItem());
            assertTrue(vm.deleteItemAction().canExecute());
        }
    }
}
