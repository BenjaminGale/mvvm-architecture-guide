package mvvm.example.shell;

import mvvm.example.shell.workspace.WorkspaceViewModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.ShellViewModel")
class ShellViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it exposes the given workspaces")
        void exposesWorkspaces() {
            var orders = new WorkspaceViewModel("Orders");
            var customers = new WorkspaceViewModel("Customers");

            var shell = new ShellViewModel(List.of(orders, customers));

            assertEquals(List.of(orders, customers), shell.workspaces());
        }

        @Test
        @DisplayName("the first workspace becomes the current workspace")
        void firstWorkspaceIsCurrent() {
            var orders = new WorkspaceViewModel("Orders");
            var customers = new WorkspaceViewModel("Customers");

            var shell = new ShellViewModel(List.of(orders, customers));

            assertEquals(orders, shell.currentWorkspaceProperty().get());
        }

        @Test
        @DisplayName("no workspace is current when the list is empty")
        void noCurrentWorkspaceWhenEmpty() {
            var shell = new ShellViewModel(List.of());

            assertNull(shell.currentWorkspaceProperty().get());
        }
    }

    @Nested
    @DisplayName("when a workspace's open action executes")
    class WhenOpenActionExecutes {

        @Test
        @DisplayName("it becomes the current workspace")
        void becomesCurrentWorkspace() {
            var orders = new WorkspaceViewModel("Orders");
            var customers = new WorkspaceViewModel("Customers");
            var shell = new ShellViewModel(List.of(orders, customers));

            customers.openAction().execute();

            assertEquals(customers, shell.currentWorkspaceProperty().get());
        }
    }
}
