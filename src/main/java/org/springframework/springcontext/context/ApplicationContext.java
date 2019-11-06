package org.springframework.springcontext.context;

import com.sun.istack.internal.Nullable;
import org.springframework.springbean.beans.factory.HierarchicalBeanFactory;
import org.springframework.springbean.beans.factory.ListableBeanFactory;
import org.springframework.springbean.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.springcore.core.env.EnvironmentCapable;
import org.springframework.springcore.core.io.ResourcePatternResolver;

public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory, MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
  @Nullable
  String getId();

  String getApplicationName();

  String getDisplayName();

  long getStartupDate();

  @Nullable
  ApplicationContext getParent();

  AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
}
