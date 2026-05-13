package mvvm.example.core.viewmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ViewModelRouter implements AppHost {

    private final Map<Class<?>, Consumer<Object>> handlers = new HashMap<>();

    @Override
    public <VM> void receive(Class<VM> vmClass, Consumer<VM> handler) {
        handlers.put(vmClass, vm -> handler.accept(vmClass.cast(vm)));
    }

    public void dispatch(Object viewModel) {
        var handler = handlers.get(viewModel.getClass());
        if (handler != null) handler.accept(viewModel);
    }
}
