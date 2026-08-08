package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.viewmodel.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Shell.WorkspaceViewModel")
class WorkspaceViewModelTest {

    private static WorkspaceTabViewModel closableTab(String title) {
        return WorkspaceTabViewModel.closable(new ReadOnlyStringWrapper(title).getReadOnlyProperty(), new TabContentViewModel(new Object()));
    }

    private static WorkspaceTabViewModel unclosableTab(String title) {
        return WorkspaceTabViewModel.unclosable(new ReadOnlyStringWrapper(title).getReadOnlyProperty(), new TabContentViewModel(new Object()));
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it has no tabs")
        void hasNoTabs() {
            var vm = new WorkspaceViewModel("Orders");

            assertEquals(0, vm.tabs().size());
        }

        @Test
        @DisplayName("it exposes the given title")
        void exposesTitle() {
            var vm = new WorkspaceViewModel("Orders");

            assertEquals("Orders", vm.titleProperty().get());
        }
    }

    @Nested
    @DisplayName("when the open action executes")
    class WhenOpenActionExecutes {

        @Test
        @DisplayName("it invokes the subscribed listener")
        void invokesListener() {
            Action.Listener onOpen = mock();
            var vm = new WorkspaceViewModel("Orders");
            vm.onOpen(onOpen);

            vm.openAction().execute();

            verify(onOpen).actionExecuted();
        }
    }

    @Nested
    @DisplayName("when opening a tab for a new key")
    class WhenOpeningATabForANewKey {

        @Test
        @DisplayName("it builds the tab via the factory and adds it")
        void addsTab() {
            var vm = new WorkspaceViewModel("Orders");
            var editor = closableTab("Order 1");

            vm.openTab("order-1", () -> editor);

            assertEquals(1, vm.tabs().size());
            assertEquals(editor, vm.tabs().getFirst());
        }

        @Test
        @DisplayName("it selects the opened tab")
        void selectsTab() {
            var vm = new WorkspaceViewModel("Orders");
            var editor = closableTab("Order 1");

            vm.openTab("order-1", () -> editor);

            assertEquals(editor, vm.selectedTabProperty().get());
        }
    }

    @Nested
    @DisplayName("when opening a tab for a key that is already open")
    class WhenOpeningATabForAnExistingKey {

        @Test
        @DisplayName("it does not invoke the factory")
        void doesNotInvokeFactory() {
            var vm = new WorkspaceViewModel("Orders");
            vm.openTab("order-1", () -> closableTab("Order 1"));

            vm.openTab("order-1", () -> {
                throw new AssertionError("factory should not be invoked for an already-open key");
            });
        }

        @Test
        @DisplayName("it does not duplicate the tab")
        void doesNotDuplicate() {
            var vm = new WorkspaceViewModel("Orders");
            vm.openTab("order-1", () -> closableTab("Order 1"));

            vm.openTab("order-1", () -> closableTab("Order 1"));

            assertEquals(1, vm.tabs().size());
        }

        @Test
        @DisplayName("it selects the existing tab")
        void selectsExistingTab() {
            var vm = new WorkspaceViewModel("Orders");
            vm.openTab("order-1", () -> closableTab("Order 1"));
            var other = closableTab("Order 2");
            vm.openTab("order-2", () -> other);

            vm.openTab("order-1", () -> closableTab("Order 1"));

            assertEquals("Order 1", vm.selectedTabProperty().get().titleProperty().get());
        }
    }

    @Nested
    @DisplayName("when a tab's close action executes")
    class WhenCloseActionExecutes {

        @Test
        @DisplayName("it removes the tab")
        void removesTab() {
            var vm = new WorkspaceViewModel("Orders");
            var editor = closableTab("Order 1");
            vm.openTab("order-1", () -> editor);

            editor.closeAction().execute();

            assertEquals(0, vm.tabs().size());
        }

        @Test
        @DisplayName("reopening the same key afterwards invokes the factory again")
        void reopeningAfterCloseInvokesFactory() {
            var vm = new WorkspaceViewModel("Orders");
            var first = closableTab("Order 1");
            vm.openTab("order-1", () -> first);
            first.closeAction().execute();

            var second = closableTab("Order 1");
            vm.openTab("order-1", () -> second);

            assertEquals(1, vm.tabs().size());
            assertEquals(second, vm.tabs().getFirst());
        }

        @Test
        @DisplayName("it throws when executed a second time")
        void throwsOnSecondExecution() {
            var vm = new WorkspaceViewModel("Orders");
            var editor = closableTab("Order 1");
            vm.openTab("order-1", () -> editor);
            editor.closeAction().execute();

            assertThrows(IllegalStateException.class, () -> editor.closeAction().execute());
        }

        @Test
        @DisplayName("it selects the previous tab")
        void selectsPreviousTab() {
            var vm = new WorkspaceViewModel("Orders");
            var first = closableTab("Order 1");
            var second = closableTab("Order 2");
            vm.openTab("order-1", () -> first);
            vm.openTab("order-2", () -> second);

            second.closeAction().execute();

            assertEquals(first, vm.selectedTabProperty().get());
        }

        @Test
        @DisplayName("closing the last tab leaves no tab selected")
        void closingLastTabClearsSelection() {
            var vm = new WorkspaceViewModel("Orders");
            var only = closableTab("Order 1");
            vm.openTab("order-1", () -> only);

            only.closeAction().execute();

            assertNull(vm.selectedTabProperty().get());
        }

        @Test
        @DisplayName("an unclosable tab's close action cannot execute")
        void unclosableTabNotClosable() {
            var vm = new WorkspaceViewModel("Orders");
            var explorer = unclosableTab("Orders");
            vm.openTab("explorer", () -> explorer);

            assertThrows(IllegalStateException.class, () -> explorer.closeAction().execute());
        }
    }
}
