package mvvm.example.core.view;

import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ViewLocator {

    private final Map<Class<?>, Function<Object, Region>> registry = new HashMap<>();
    private final Map<Class<?>, Function<Object, Dialog<Runnable>>> dialogRegistry = new HashMap<>();

    public <VM> void register(Class<VM> vmClass, Function<VM, Region> factory) {
        registry.put(vmClass, vm -> factory.apply(vmClass.cast(vm)));
    }

    public <VM> void registerDialog(Class<VM> vmClass, Function<VM, Dialog<Runnable>> factory) {
        dialogRegistry.put(vmClass, vm -> factory.apply(vmClass.cast(vm)));
    }

    public Region resolve(Object viewModel) {
        var factory = registry.get(viewModel.getClass());

        if (factory == null) {
            throw new IllegalStateException("No view registered for " + viewModel.getClass().getSimpleName());
        }

        return factory.apply(viewModel);
    }

    public Dialog<Runnable> locateDialog(Object viewModel) {
        var factory = dialogRegistry.get(viewModel.getClass());

        if (factory == null) {
            throw new IllegalStateException("No dialog registered for " + viewModel.getClass().getSimpleName());
        }

        return factory.apply(viewModel);
    }
}
