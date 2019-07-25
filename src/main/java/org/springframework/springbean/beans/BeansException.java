package org.springframework.springbean.beans;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.core.NestedRuntimeException;

public abstract class BeansException extends NestedRuntimeException {
    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}