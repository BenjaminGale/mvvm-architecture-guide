package mvvm.example.stock.domain.commands;

import mvvm.example.stock.domain.StockRepository;

public class DeleteStockAllocationsCommand {

    private final StockRepository stockRepository;

    public DeleteStockAllocationsCommand(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void execute(String productId, String orderId) {
        stockRepository.findByOrderId(orderId).stream()
            .filter(a -> a.productId().equals(productId))
            .forEach(a -> stockRepository.delete(a.id()));
    }
}
