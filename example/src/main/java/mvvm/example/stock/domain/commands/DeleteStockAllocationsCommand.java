package mvvm.example.stock.domain.commands;

import mvvm.example.stock.domain.StockRepository;

import java.util.UUID;

public class DeleteStockAllocationsCommand {

    private final StockRepository stockRepository;

    public DeleteStockAllocationsCommand(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void execute(UUID productId, UUID orderId) {
        stockRepository.findByOrderId(orderId).stream()
            .filter(a -> a.productId().equals(productId))
            .forEach(a -> stockRepository.delete(a.id()));
    }
}
