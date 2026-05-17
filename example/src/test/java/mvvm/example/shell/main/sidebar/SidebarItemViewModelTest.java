package mvvm.example.shell.main.sidebar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.SidebarItemViewModel")
class SidebarItemViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it has the expected title")
        void showsTitle() {
            var vm = new SidebarItemViewModel("Orders", () -> {});

            assertEquals("Orders", vm.titleProperty().get());
        }

        @Test
        @DisplayName("it can open its workspace when clicked")
        void actionIsExecutable() {
            var vm = new SidebarItemViewModel("Orders", () -> {});

            assertTrue(vm.openWorkspaceAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when the navigation item is clicked")
    class WhenWorkspaceIsOpened {

        @Test
        @DisplayName("it workspace is opened")
        void invokesCallback() {
            var invoked = new boolean[]{false};
            var vm = new SidebarItemViewModel("Orders", () -> invoked[0] = true);

            vm.openWorkspaceAction().execute();

            assertTrue(invoked[0]);
        }
    }
}
