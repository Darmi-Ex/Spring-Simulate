package org.springframework.springbean.beans;

import org.jetbrains.annotations.Nullable;

public class FatalBeanException extends BeansException {
    public FatalBeanException(String msg) {
        super(msg);
    }

    public FatalBeanException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}