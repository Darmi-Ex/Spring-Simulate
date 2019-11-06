package org.springframework.springbean.beans.factory;

import com.sun.istack.internal.Nullable;
import org.springframework.springbean.beans.BeansException;
import org.springframework.springcore.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {
  boolean containsBeanDefinition(String var1);

  int getBeanDefinitionCount();

  String[] getBeanDefinitionNames();

  String[] getBeanNamesForType(ResolvableType var1);

  String[] getBeanNamesForType(@Nullable Class<?> var1);

  String[] getBeanNamesForType(@Nullable Class<?> var1, boolean var2, boolean var3);

  <T> Map<String, T> getBeansOfType(@Nullable Class<T> var1) throws BeansException;

  <T> Map<String, T> getBeansOfType(@Nullable Class<T> var1, boolean var2, boolean var3) throws BeansException;

  String[] getBeanNamesForAnnotation(Class<? extends Annotation> var1);

  Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> var1) throws BeansException;

  @Nullable
  <A extends Annotation> A findAnnotationOnBean(String var1, Class<A> var2) throws NoSuchBeanDefinitionException;
}