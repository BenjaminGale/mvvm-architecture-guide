package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.viewmodel.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Shell.WorkspaceViewModel")
class WorkspaceViewModelTest {

    private static WorkspaceTab tab(String title, boolean closable) {
        return new WorkspaceTab() {
            private final ReadOnlyStringWrapper title_ = new ReadOnlyStringWrapper(title);
            private Action.Listener onClose = () -> {};
            private final Action closeAction = closable
                ? new Action(() -> onClose.actionExecuted())
                : Action.disabled();

            @Override public ReadOnlyStringProperty titleProperty() { return title_.getReadOnlyProperty(); }
            @Override public Action closeAction() { return closeAction; }
            @Override public void onClose(Action.Listener listener) { this.onClose = listener; }
        };
    }

    private static WorkspaceTab closableTab(String title) {
        return tab(title, true);
    }

    private static WorkspaceTab pinnedTab(String title) {
        return tab(title, false);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it has the explorer tab as its only tab")
        void hasExplorerTabOnly() {
            var explorer = pinnedTab("Orders");
            var vm = new WorkspaceViewModel("Orders", explorer);

            assertEquals(1, vm.tabs().size());
            assertEquals(explorer, vm.tabs().getFirst());
        }

        @Test
        @DisplayName("the explorer tab is selected")
        void selectsExplorerTab() {
            var explorer = pinnedTab("Orders");
            var vm = new WorkspaceViewModel("Orders", explorer);

            assertEquals(explorer, vm.selectedTabProperty().get());
        }

        @Test
        @DisplayName("it exposes the given title")
        void exposesTitle() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));

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
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            vm.onOpen(onOpen);

            vm.openAction().execute();

            verify(onOpen).actionExecuted();
        }
    }

    @Nested
    @DisplayName("when opening a tab")
    class WhenOpeningATab {

        @Test
        @DisplayName("it adds the tab after the existing tabs")
        void addsTab() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            var editor = closableTab("Order 1");

            vm.openTab(editor);

            assertEquals(2, vm.tabs().size());
            assertEquals(editor, vm.tabs().get(1));
        }

        @Test
        @DisplayName("it selects the opened tab")
        void selectsTab() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            var editor = closableTab("Order 1");

            vm.openTab(editor);

            assertEquals(editor, vm.selectedTabProperty().get());
        }

        @Test
        @DisplayName("reopening an already-open tab does not duplicate it")
        void reopeningDoesNotDuplicate() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            var editor = closableTab("Order 1");
            vm.openTab(editor);

            vm.openTab(editor);

            assertEquals(2, vm.tabs().size());
        }
    }

    @Nested
    @DisplayName("when a tab's close action executes")
    class WhenCloseActionExecutes {

        @Test
        @DisplayName("it removes the tab")
        void removesTab() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            var editor = closableTab("Order 1");
            vm.openTab(editor);

            editor.closeAction().execute();

            assertEquals(1, vm.tabs().size());
        }

        @Test
        @DisplayName("it throws when executed a second time")
        void throwsOnSecondExecution() {
            var vm = new WorkspaceViewModel("Orders", pinnedTab("Orders"));
            var editor = closableTab("Order 1");
            vm.openTab(editor);
            editor.closeAction().execute();

            assertThrows(IllegalStateException.class, () -> editor.closeAction().execute());
        }

        @Test
        @DisplayName("it selects the previous tab")
        void selectsPreviousTab() {
            var explorer = pinnedTab("Orders");
            var vm = new WorkspaceViewModel("Orders", explorer);
            var first = closableTab("Order 1");
            var second = closableTab("Order 2");
            vm.openTab(first);
            vm.openTab(second);

            second.closeAction().execute();

            assertEquals(first, vm.selectedTabProperty().get());
        }

        @Test
        @DisplayName("the explorer tab's close action cannot execute")
        void explorerTabNotClosable() {
            var explorer = pinnedTab("Orders");
            new WorkspaceViewModel("Orders", explorer);

            assertThrows(IllegalStateException.class, () -> explorer.closeAction().execute());
        }
    }
}
