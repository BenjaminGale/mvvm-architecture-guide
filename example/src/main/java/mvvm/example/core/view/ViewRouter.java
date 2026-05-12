package mvvm.example.core.view;

import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ViewRouter {

    private final ViewLocator viewLocator;
    private final Map<Class<?>, Consumer<Region>> listeners = new HashMap<>();

    public ViewRouter(ViewLocator viewLocator) {
        this.viewLocator = viewLocator;
    }

    public <V extends Region> void addListener(Class<V> viewClass, Consumer<V> listener) {
        listeners.put(viewClass, view -> listener.accept(viewClass.cast(view)));
    }

    public void route(Object viewModel) {
        var view = viewLocator.resolve(viewModel);
        var listener = listeners.get(view.getClass());
        if (listener != null) listener.accept(view);
    }
}
