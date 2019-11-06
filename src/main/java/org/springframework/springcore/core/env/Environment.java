package org.springframework.springcore.core.env;

public interface Environment extends PropertyResolver {
  String[] getActiveProfiles();

  String[] getDefaultProfiles();

  boolean acceptsProfiles(String... var1);
}