package org.springframework.springcontext.context.annotion;

public interface AnnotationConfigRegistry {
    void register(Class<?>... var1);

    void scan(String... var1);
}
