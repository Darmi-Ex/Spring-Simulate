package org.springframework.springbean.beans.factory;

import org.jetbrains.annotations.Nullable;
import org.springframework.springbean.beans.FatalBeanException;

public class BeanDefinitionStoreException extends FatalBeanException {
    @Nullable
    private String resourceDescription;
    @Nullable
    private String beanName;

    public BeanDefinitionStoreException(String msg) {
        super(msg);
    }

    public BeanDefinitionStoreException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

    public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg) {
        super(msg);
        this.resourceDescription = resourceDescription;
    }

    public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.resourceDescription = resourceDescription;
    }

    public BeanDefinitionStoreException(@Nullable String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, (Throwable)null);
    }

    public BeanDefinitionStoreException(@Nullable String resourceDescription, String beanName, String msg, @Nullable Throwable cause) {
        super("Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
    }

    @Nullable
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    @Nullable
    public String getBeanName() {
        return this.beanName;
    }
}
