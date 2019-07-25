package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;

public interface AttributeAccessor {
    void setAttribute(String var1, @Nullable Object var2);

    @Nullable
    Object getAttribute(String var1);

    @Nullable
    Object removeAttribute(String var1);

    boolean hasAttribute(String var1);

    String[] attributeNames();
}
