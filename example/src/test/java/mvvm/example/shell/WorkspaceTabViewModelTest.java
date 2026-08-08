package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.WorkspaceTabViewModel")
class WorkspaceTabTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it exposes the given title")
        void exposesTitle() {
            var title = new ReadOnlyStringWrapper("Order 1");
            var tab = WorkspaceTabViewModel.closable(title.getReadOnlyProperty(), new Object());

            assertEquals("Order 1", tab.titleProperty().get());
        }

        @Test
        @DisplayName("it exposes the given content")
        void exposesContent() {
            var content = new Object();
            var tab = WorkspaceTabViewModel.closable(new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), content);

            assertEquals(content, tab.content());
        }

        @Test
        @DisplayName("its title tracks the given observable")
        void titleTracksObservable() {
            var title = new ReadOnlyStringWrapper("New Order");
            var tab = WorkspaceTabViewModel.closable(title.getReadOnlyProperty(), new Object());

            title.set("Order 1234");

            assertEquals("Order 1234", tab.titleProperty().get());
        }
    }

    @Nested
    @DisplayName("when created as closable")
    class WhenClosable {

        @Test
        @DisplayName("its close action can execute")
        void closeActionCanExecute() {
            var tab = WorkspaceTabViewModel.closable(new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), new Object());

            assertTrue(tab.closeAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when created as pinned")
    class WhenPinned {

        @Test
        @DisplayName("its close action cannot execute")
        void closeActionCannotExecute() {
            var tab = WorkspaceTabViewModel.pinned(new ReadOnlyStringWrapper("Orders").getReadOnlyProperty(), new Object());

            assertFalse(tab.closeAction().canExecute());
        }
    }
}
