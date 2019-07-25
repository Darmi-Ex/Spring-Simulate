package org.springframework.springcore.core.io;

import com.sun.istack.internal.Nullable;

public interface ResourceLoader {
    String CLASSPATH_URL_PREFIX = "classpath:";

    Resource getResource(String var1);

    @Nullable
    ClassLoader getClassLoader();
}
