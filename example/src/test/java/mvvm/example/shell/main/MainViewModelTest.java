package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import mvvm.example.shell.main.sidebar.SidebarViewModel;
import mvvm.example.shell.main.statusbar.StatusBarViewModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.MainViewModel")
class MainViewModelTest {

    private static final SidebarViewModel SIDEBAR = new SidebarViewModel(FXCollections.observableArrayList());
    private static final StatusBarViewModel STATUS_BAR = new StatusBarViewModel(FXCollections.observableArrayList());

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it exposes the sidebar")
        void exposesSidebar() {
            var workspace = new ReadOnlyObjectWrapper<>();
            var vm = new MainViewModel(SIDEBAR, STATUS_BAR, workspace.getReadOnlyProperty());

            assertEquals(SIDEBAR, vm.sidebar());
        }

        @Test
        @DisplayName("it exposes the status bar")
        void exposesStatusBar() {
            var workspace = new ReadOnlyObjectWrapper<>();
            var vm = new MainViewModel(SIDEBAR, STATUS_BAR, workspace.getReadOnlyProperty());

            assertEquals(STATUS_BAR, vm.statusBar());
        }

        @Test
        @DisplayName("it shows the current workspace")
        void showsCurrentWorkspace() {
            var workspace = new ReadOnlyObjectWrapper<Object>("initial");
            var vm = new MainViewModel(SIDEBAR, STATUS_BAR, workspace.getReadOnlyProperty());

            assertEquals("initial", vm.currentWorkspaceProperty().get());
        }
    }

    @Nested
    @DisplayName("when the workspace changes")
    class WhenWorkspaceChanges {

        static Stream<Arguments> workspaceChangeCases() {
            return Stream.of(
                Arguments.of("after the workspace is replaced", "initial", "updated", "updated"),
                Arguments.of("after the workspace is cleared", "initial", null, null),
                Arguments.of("after the workspace changes multiple times", "initial", "second", "second")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("workspaceChangeCases")
        @DisplayName("it shows the updated workspace")
        void showsUpdatedWorkspace(String caseName, Object initial, Object updated, Object expected) {
            var workspace = new ReadOnlyObjectWrapper<>(initial);
            var vm = new MainViewModel(SIDEBAR, STATUS_BAR, workspace.getReadOnlyProperty());

            workspace.set(updated);

            assertEquals(expected, vm.currentWorkspaceProperty().get());
        }
    }
}
