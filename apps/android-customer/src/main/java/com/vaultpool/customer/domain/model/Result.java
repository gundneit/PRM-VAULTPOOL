package com.vaultpool.customer.domain.model;

/**
 * Generic Result class for handling success and failure states.
 * Used throughout the app to handle operations that can fail.
 *
 * @param <T> The type of data in case of success
 */
public class Result<T> {

    private final T data;
    private final Throwable error;
    private final boolean success;

    private Result(T data, Throwable error, boolean success) {
        this.data = data;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }

    public static <T> Result<T> failure(Throwable error) {
        return new Result<>(null, error, false);
    }

    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(null, new Exception(errorMessage), false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorMessage() {
        return error != null ? error.getMessage() : null;
    }

    public void fold(java.util.function.Consumer<T> onSuccess, java.util.function.Consumer<Throwable> onFailure) {
        if (success && data != null) {
            onSuccess.accept(data);
        } else if (!success && error != null) {
            onFailure.accept(error);
        }
    }
}
