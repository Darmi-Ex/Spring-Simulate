package org.springframework.springbean.beans.factory.support;

import org.springframework.springbean.beans.factory.BeanDefinitionStoreException;
import org.springframework.springbean.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.springbean.beans.factory.config.BeanDefinition;
import org.springframework.springcore.core.AliasRegistry;

public interface BeanDefinitionRegistry extends AliasRegistry {
    void registerBeanDefinition(String var1, BeanDefinition var2) throws BeanDefinitionStoreException;

    void removeBeanDefinition(String var1) throws NoSuchBeanDefinitionException;

    BeanDefinition getBeanDefinition(String var1) throws NoSuchBeanDefinitionException;

    boolean containsBeanDefinition(String var1);

    String[] getBeanDefinitionNames();

    int getBeanDefinitionCount();

    boolean isBeanNameInUse(String var1);
}
