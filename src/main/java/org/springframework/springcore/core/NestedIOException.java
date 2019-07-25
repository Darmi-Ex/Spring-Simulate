package org.springframework.springcore.core;

import com.sun.istack.internal.Nullable;

import java.io.IOException;

public class NestedIOException extends IOException {
    public NestedIOException(String msg) {
        super(msg);
    }

    public NestedIOException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

    @Nullable
    public String getMessage() {
        return NestedExceptionUtils.buildMessage(super.getMessage(), this.getCause());
    }

    static {
        NestedExceptionUtils.class.getName();
    }
}