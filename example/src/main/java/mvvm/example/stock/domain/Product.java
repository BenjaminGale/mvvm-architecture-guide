package mvvm.example.stock.domain;

import java.math.BigDecimal;

public record Product(String id, String name, BigDecimal unitPrice, int quantityInStock) {}
