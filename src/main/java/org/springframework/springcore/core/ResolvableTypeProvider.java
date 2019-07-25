package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;

public interface ResolvableTypeProvider {
    @Nullable
    ResolvableType getResolvableType();
}