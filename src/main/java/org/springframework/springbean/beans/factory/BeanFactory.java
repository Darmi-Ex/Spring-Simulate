package org.springframework.springbean.beans.factory;

import com.sun.istack.internal.Nullable;
import org.springframework.springbean.beans.BeansException;
import org.springframework.springcore.core.ResolvableType;

public interface BeanFactory {
  String FACTORY_BEAN_PREFIX = "&";

  Object getBean(String var1) throws BeansException;

  <T> T getBean(String var1, @Nullable Class<T> var2) throws BeansException;

  Object getBean(String var1, Object... var2) throws BeansException;

  <T> T getBean(Class<T> var1) throws BeansException;

  <T> T getBean(Class<T> var1, Object... var2) throws BeansException;

  boolean containsBean(String var1);

  boolean isSingleton(String var1) throws NoSuchBeanDefinitionException;

  boolean isPrototype(String var1) throws NoSuchBeanDefinitionException;

  boolean isTypeMatch(String var1, ResolvableType var2) throws NoSuchBeanDefinitionException;

  boolean isTypeMatch(String var1, @Nullable Class<?> var2) throws NoSuchBeanDefinitionException;

  @Nullable
  Class<?> getType(String var1) throws NoSuchBeanDefinitionException;

  String[] getAliases(String var1);
}