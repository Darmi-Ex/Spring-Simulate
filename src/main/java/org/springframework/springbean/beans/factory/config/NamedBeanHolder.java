package org.springframework.springbean.beans.factory.config;

import org.springframework.springbean.beans.factory.NamedBean;
import org.springframework.springcore.utils.Assert;

public class NamedBeanHolder<T> implements NamedBean {
  private final String beanName;
  private final T beanInstance;

  public NamedBeanHolder(String beanName, T beanInstance) {
    Assert.notNull(beanName, "Bean name must not be null");
    this.beanName = beanName;
    this.beanInstance = beanInstance;
  }

  public String getBeanName() {
    return this.beanName;
  }

  public T getBeanInstance() {
    return this.beanInstance;
  }
}