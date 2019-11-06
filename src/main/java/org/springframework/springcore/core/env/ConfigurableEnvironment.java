package org.springframework.springcore.core.env;

import java.util.Map;

public interface ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver {
  void setActiveProfiles(String... var1);

  void addActiveProfile(String var1);

  void setDefaultProfiles(String... var1);

  MutablePropertySources getPropertySources();

  Map<String, Object> getSystemEnvironment();

  Map<String, Object> getSystemProperties();

  void merge(ConfigurableEnvironment var1);
}