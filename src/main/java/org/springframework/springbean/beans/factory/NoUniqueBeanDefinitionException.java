package org.springframework.springbean.beans.factory;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.StringUtils;

import java.util.Arrays;
import java.util.Collection;

public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {
  private int numberOfBeansFound;
  @Nullable
  private Collection<String> beanNamesFound;

  public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
    super(type, message);
    this.numberOfBeansFound = numberOfBeansFound;
  }

  public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
    this(type, beanNamesFound.size(), "expected single matching bean but found " + beanNamesFound.size() + ": " + StringUtils.collectionToCommaDelimitedString(beanNamesFound));
    this.beanNamesFound = beanNamesFound;
  }

  public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
    this(type, (Collection) Arrays.asList(beanNamesFound));
  }

  public int getNumberOfBeansFound() {
    return this.numberOfBeansFound;
  }

  @Nullable
  public Collection<String> getBeanNamesFound() {
    return this.beanNamesFound;
  }
}
