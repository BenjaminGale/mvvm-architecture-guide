package mvvm.example.orders.domain;

import mvvm.example.stock.domain.StockRepository;

public class DeleteLineItemCommand {

    private final StockRepository stockRepository;

    public DeleteLineItemCommand(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void execute(String productId, String orderId) {
        stockRepository.findByOrderId(orderId).stream()
            .filter(a -> a.productId().equals(productId))
            .forEach(a -> stockRepository.delete(a.id()));
    }
}
