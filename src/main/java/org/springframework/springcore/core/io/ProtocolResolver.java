package org.springframework.springcore.core.io;

import com.sun.istack.internal.Nullable;

@FunctionalInterface
public interface ProtocolResolver {
    @Nullable
    Resource resolve(String var1, ResourceLoader var2);
}