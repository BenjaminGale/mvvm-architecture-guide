package mvvm.example.core.viewmodel;

import java.util.function.Consumer;

public interface AppHost {
    <VM> void receive(Class<VM> vmClass, Consumer<VM> handler);
}
