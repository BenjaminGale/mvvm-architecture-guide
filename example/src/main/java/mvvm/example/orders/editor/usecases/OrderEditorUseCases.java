package mvvm.example.orders.editor.usecases;

public record OrderEditorUseCases(
    SaveOrderUseCase save,
    CopyOrderUseCase copy,
    DeleteOrderUseCase delete
) {}
