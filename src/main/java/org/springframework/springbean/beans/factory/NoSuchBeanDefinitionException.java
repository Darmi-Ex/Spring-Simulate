package org.springframework.springbean.beans.factory;

import org.jetbrains.annotations.Nullable;
import org.springframework.springbean.beans.BeansException;
import org.springframework.springcore.core.ResolvableType;

public class NoSuchBeanDefinitionException extends BeansException {
    @Nullable
    private String beanName;
    @Nullable
    private ResolvableType resolvableType;

    public NoSuchBeanDefinitionException(String name) {
        super("No bean named '" + name + "' available");
        this.beanName = name;
    }

    public NoSuchBeanDefinitionException(String name, String message) {
        super("No bean named '" + name + "' available: " + message);
        this.beanName = name;
    }

    public NoSuchBeanDefinitionException(Class<?> type) {
        this(ResolvableType.forClass(type));
    }

    public NoSuchBeanDefinitionException(Class<?> type, String message) {
        this(ResolvableType.forClass(type), message);
    }

    public NoSuchBeanDefinitionException(ResolvableType type) {
        super("No qualifying bean of type '" + type + "' available");
        this.resolvableType = type;
    }

    public NoSuchBeanDefinitionException(ResolvableType type, String message) {
        super("No qualifying bean of type '" + type + "' available: " + message);
        this.resolvableType = type;
    }

    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    @Nullable
    public Class<?> getBeanType() {
        return this.resolvableType != null ? this.resolvableType.resolve() : null;
    }

    @Nullable
    public ResolvableType getResolvableType() {
        return this.resolvableType;
    }

    public int getNumberOfBeansFound() {
        return 0;
    }
}
