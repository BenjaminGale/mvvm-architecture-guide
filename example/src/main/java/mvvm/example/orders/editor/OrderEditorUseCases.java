package mvvm.example.orders.editor;

public record OrderEditorUseCases(
    SaveOrderUseCase save,
    CopyOrderUseCase copy,
    DeleteOrderUseCase delete
) {}
