package mvvm.example.shell.tabs;

import javafx.beans.property.ReadOnlyStringWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.TabViewModel")
class TabViewModelTest {

    private static TabContentViewModel content() {
        return new TabContentViewModel(new Object());
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it exposes the given title")
        void exposesTitle() {
            var title = new ReadOnlyStringWrapper("Order 1");
            var tab = TabViewModel.closable(title.getReadOnlyProperty(), content());

            assertEquals("Order 1", tab.titleProperty().get());
        }

        @Test
        @DisplayName("it exposes the given content")
        void exposesContent() {
            var content = content();
            var tab = TabViewModel.closable(new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), content);

            assertEquals(content, tab.content());
        }

        @Test
        @DisplayName("its title tracks the given observable")
        void titleTracksObservable() {
            var title = new ReadOnlyStringWrapper("New Order");
            var tab = TabViewModel.closable(title.getReadOnlyProperty(), content());

            title.set("Order 1234");

            assertEquals("Order 1234", tab.titleProperty().get());
        }
    }

    @Nested
    @DisplayName("when created as unclosable")
    class WhenUnclosable {

        @Test
        @DisplayName("its close action cannot execute")
        void closeActionCannotExecute() {
            var tab = TabViewModel.unclosable(new ReadOnlyStringWrapper("Orders").getReadOnlyProperty(), content());

            assertFalse(tab.closeAction().canExecute());
        }
    }

    @Nested
    @DisplayName("when created as closable with no veto")
    class WhenClosableWithNoVeto {

        @Test
        @DisplayName("its close action can execute")
        void closeActionCanExecute() {
            var tab = TabViewModel.closable(new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), content());

            assertTrue(tab.closeAction().canExecute());
        }

        @Test
        @DisplayName("executing the close action notifies the subscribed listener")
        void notifiesListener() {
            var closed = new boolean[1];
            var tab = TabViewModel.closable(new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), content());
            tab.onClose(() -> closed[0] = true);

            tab.closeAction().execute();

            assertTrue(closed[0]);
        }
    }

    @Nested
    @DisplayName("when created as closable with a veto that returns false")
    class WhenClosableAndVetoed {

        @Test
        @DisplayName("executing the close action does not notify the subscribed listener")
        void doesNotNotifyListener() {
            var closed = new boolean[1];
            var tab = TabViewModel.closable(
                new ReadOnlyStringWrapper("Order 1").getReadOnlyProperty(), content(), () -> false);
            tab.onClose(() -> closed[0] = true);

            tab.closeAction().execute();

            assertFalse(closed[0]);
        }
    }
}
