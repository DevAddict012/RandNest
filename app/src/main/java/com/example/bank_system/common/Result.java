package com.example.bank_system.common;

/**
 * A typed result that carries either a success payload or a failure code.
 *
 * Usage:
 *   Result<String> r = Result.ok("hello");
 *   Result<Void>   e = Result.error(ResultCode.VALIDATION_ERROR, "pin_too_short");
 *
 * Rules:
 *  - Never infer success from button activation or intent creation; always
 *    check isOk() on the result returned from a service/repository.
 *  - messageKey is a string-resource key (e.g. "error_pin_too_short"),
 *    never a raw SQL message or stack trace.
 */
public final class Result<T> {

    private final ResultCode code;
    private final T          value;
    private final String     messageKey;

    private Result(ResultCode code, T value, String messageKey) {
        this.code       = code;
        this.value      = value;
        this.messageKey = messageKey;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    public static <T> Result<T> ok(T value) {
        return new Result<>(ResultCode.OK, value, null);
    }

    /** Convenience for void success (e.g. Result<Void>). */
    public static Result<Void> ok() {
        return new Result<>(ResultCode.OK, null, null);
    }

    public static <T> Result<T> error(ResultCode code, String messageKey) {
        return new Result<>(code, null, messageKey);
    }

    public static <T> Result<T> error(ResultCode code) {
        return new Result<>(code, null, null);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public boolean isOk() {
        return code == ResultCode.OK;
    }

    public ResultCode getCode() {
        return code;
    }

    /** Non-null only when isOk() == true and a value was provided. */
    public T getValue() {
        return value;
    }

    /** String-resource key for a user-visible error message; may be null. */
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", messageKey=" + messageKey + "}";
    }
}
