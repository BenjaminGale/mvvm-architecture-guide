package mvvm.example.customers.domain;

import java.util.UUID;

public record Customer(UUID id, String name, String email, CustomerStatus status) {}
