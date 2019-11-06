package org.springframework.springcore.core.convert;

import org.jetbrains.annotations.Nullable;

public interface ConversionService {
  boolean canConvert(@Nullable Class<?> var1, Class<?> var2);

  boolean canConvert(@Nullable TypeDescriptor var1, TypeDescriptor var2);

  @Nullable
  <T> T convert(@Nullable Object var1, Class<T> var2);

  @Nullable
  Object convert(@Nullable Object var1, @Nullable TypeDescriptor var2, TypeDescriptor var3);
}