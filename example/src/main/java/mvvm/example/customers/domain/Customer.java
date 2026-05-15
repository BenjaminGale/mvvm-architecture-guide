package mvvm.example.customers.domain;

public record Customer(String id, String name, String email, CustomerStatus status) {}
