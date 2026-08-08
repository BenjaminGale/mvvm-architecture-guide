package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.ShellViewModel")
class ShellViewModelTest {

    private static WorkspaceTabViewModel tab(String title) {
        return WorkspaceTabViewModel.pinned(new ReadOnlyStringWrapper(title).getReadOnlyProperty(), new Object());
    }

    @Nested
    @DisplayName("when a workspace is registered")
    class WhenWorkspaceRegistered {

        @Test
        @DisplayName("it is added to the workspaces list")
        void addsToWorkspaces() {
            var shell = new ShellViewModel();

            shell.registerWorkspace("Orders", tab("Orders"));

            assertEquals(1, shell.workspaces().size());
            assertEquals("Orders", shell.workspaces().getFirst().titleProperty().get());
        }

        @Test
        @DisplayName("the first registered workspace becomes the current workspace")
        void firstWorkspaceIsCurrent() {
            var shell = new ShellViewModel();

            shell.registerWorkspace("Orders", tab("Orders"));

            assertEquals(shell.workspaces().getFirst(), shell.currentWorkspaceProperty().get());
        }

        @Test
        @DisplayName("subsequently registered workspaces do not become current")
        void laterWorkspacesAreNotCurrent() {
            var shell = new ShellViewModel();

            shell.registerWorkspace("Orders", tab("Orders"));
            shell.registerWorkspace("Customers", tab("Customers"));

            assertEquals(shell.workspaces().getFirst(), shell.currentWorkspaceProperty().get());
        }
    }

    @Nested
    @DisplayName("when a workspace's open action executes")
    class WhenOpenActionExecutes {

        @Test
        @DisplayName("it becomes the current workspace")
        void becomesCurrentWorkspace() {
            var shell = new ShellViewModel();
            shell.registerWorkspace("Orders", tab("Orders"));
            shell.registerWorkspace("Customers", tab("Customers"));
            var customers = shell.workspaces().get(1);

            customers.openAction().execute();

            assertEquals(customers, shell.currentWorkspaceProperty().get());
        }
    }
}
