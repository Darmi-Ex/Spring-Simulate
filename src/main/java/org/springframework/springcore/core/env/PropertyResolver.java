package org.springframework.springcore.core.env;

import com.sun.istack.internal.Nullable;

public interface PropertyResolver {
  boolean containsProperty(String var1);

  @Nullable
  String getProperty(String var1);

  String getProperty(String var1, String var2);

  @Nullable
  <T> T getProperty(String var1, Class<T> var2);

  <T> T getProperty(String var1, Class<T> var2, T var3);

  String getRequiredProperty(String var1) throws IllegalStateException;

  <T> T getRequiredProperty(String var1, Class<T> var2) throws IllegalStateException;

  String resolvePlaceholders(String var1);

  String resolveRequiredPlaceholders(String var1) throws IllegalArgumentException;
}
