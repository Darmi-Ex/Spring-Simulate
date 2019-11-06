package org.springframework.springbean.beans.factory;

import org.jetbrains.annotations.Nullable;

public interface HierarchicalBeanFactory extends BeanFactory {
  @Nullable
  BeanFactory getParentBeanFactory();

  boolean containsLocalBean(String var1);
}