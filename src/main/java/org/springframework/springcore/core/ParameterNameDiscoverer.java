package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface ParameterNameDiscoverer {
    @Nullable
    String[] getParameterNames(Method var1);

    @Nullable
    String[] getParameterNames(Constructor<?> var1);
}
