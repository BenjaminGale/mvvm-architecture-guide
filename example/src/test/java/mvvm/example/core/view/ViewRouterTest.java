package mvvm.example.core.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewRouter")
class ViewRouterTest {

    private static class StubViewModel {}
    private static class StubView extends Pane {}
    private static class OtherViewModel {}
    private static class OtherView extends Pane {}

    private ViewRouter routerWith(Class<?> vmClass, Region view) {
        var locator = new ViewLocator();
        locator.register(StubViewModel.class, vm -> view);
        var router = new ViewRouter(locator);
        return router;
    }

    @Nested
    @DisplayName("when a listener is registered for a view type")
    class WhenListenerRegistered {

        @Test
        @DisplayName("the listener is called when routing to a matching ViewModel")
        void listenerCalledForMatchingViewModel() {
            var view = new StubView();
            var locator = new ViewLocator();
            locator.register(StubViewModel.class, vm -> view);
            var router = new ViewRouter(locator);
            var received = new AtomicReference<Region>();
            router.addListener(StubView.class, received::set);

            router.route(new StubViewModel());

            assertSame(view, received.get());
        }

        @Test
        @DisplayName("the listener receives the correctly typed view")
        void listenerReceivesCorrectlyTypedView() {
            var view = new StubView();
            var locator = new ViewLocator();
            locator.register(StubViewModel.class, vm -> view);
            var router = new ViewRouter(locator);
            var received = new AtomicReference<StubView>();
            router.addListener(StubView.class, received::set);

            router.route(new StubViewModel());

            assertInstanceOf(StubView.class, received.get());
        }
    }

    @Nested
    @DisplayName("when no listener is registered for the resolved view type")
    class WhenNoListenerRegistered {

        @Test
        @DisplayName("routing completes without error")
        void routingCompletesWithoutError() {
            var locator = new ViewLocator();
            locator.register(StubViewModel.class, vm -> new StubView());
            var router = new ViewRouter(locator);

            assertDoesNotThrow(() -> router.route(new StubViewModel()));
        }

        @Test
        @DisplayName("listeners registered for other view types are not called")
        void otherListenersNotCalled() {
            var locator = new ViewLocator();
            locator.register(StubViewModel.class, vm -> new StubView());
            locator.register(OtherViewModel.class, vm -> new OtherView());
            var router = new ViewRouter(locator);
            var received = new AtomicReference<Region>();
            router.addListener(OtherView.class, received::set);

            router.route(new StubViewModel());

            assertNull(received.get());
        }
    }
}
