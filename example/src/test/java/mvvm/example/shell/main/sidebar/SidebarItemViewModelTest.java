package mvvm.example.shell.main.sidebar;

import javafx.beans.property.ReadOnlyIntegerWrapper;
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
        @DisplayName("it has a count of zero")
        void initialCountIsZero() {
            var vm = new SidebarItemViewModel("Orders", () -> {});

            assertEquals(0, vm.countProperty().get());
        }

        @Test
        @DisplayName("it can open its workspace when clicked")
        void actionIsExecutable() {
            var vm = new SidebarItemViewModel("Orders", () -> {});

            assertTrue(vm.openWorkspaceAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when created with a count property")
    class WhenCreatedWithCount {

        @Test
        @DisplayName("it reflects the initial count")
        void reflectsInitialCount() {
            var count = new ReadOnlyIntegerWrapper(3);
            var vm = new SidebarItemViewModel("Orders", () -> {}, count.getReadOnlyProperty());

            assertEquals(3, vm.countProperty().get());
        }

        @Test
        @DisplayName("it updates when the count changes")
        void updatesWhenCountChanges() {
            var count = new ReadOnlyIntegerWrapper(0);
            var vm = new SidebarItemViewModel("Orders", () -> {}, count.getReadOnlyProperty());

            count.set(5);

            assertEquals(5, vm.countProperty().get());
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
