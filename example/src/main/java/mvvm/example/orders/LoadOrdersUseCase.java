package mvvm.example.orders;

import java.util.List;

@FunctionalInterface
public interface LoadOrdersUseCase {
    List<Order> execute();
}
