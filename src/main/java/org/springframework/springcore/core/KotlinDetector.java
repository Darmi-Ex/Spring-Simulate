package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.ClassUtils;

import java.lang.annotation.Annotation;

public abstract class KotlinDetector {
    @Nullable
    private static final Class<? extends Annotation> kotlinMetadata;

    public KotlinDetector() {
    }

    public static boolean isKotlinPresent() {
        return kotlinMetadata != null;
    }

    public static boolean isKotlinType(Class<?> clazz) {
        return kotlinMetadata != null && clazz.getDeclaredAnnotation(kotlinMetadata) != null;
    }

    static {
        Class metadata;
        try {
            metadata = ClassUtils.forName("kotlin.Metadata", KotlinDetector.class.getClassLoader());
        } catch (ClassNotFoundException var2) {
            metadata = null;
        }

        kotlinMetadata = metadata;
    }
}