package mvvm.example.orders.explorer;

import mvvm.example.orders.Order;

import java.util.List;

@FunctionalInterface
public interface LoadOrdersUseCase {
    List<Order> execute();
}
