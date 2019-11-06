package org.springframework.springbean.beans;

import com.sun.corba.se.impl.io.TypeMismatchException;
import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.core.MethodParameter;
import java.lang.reflect.Field;
public interface TypeConverter {
  @Nullable
  <T> T convertIfNecessary(@Nullable Object var1, @Nullable Class<T> var2) throws TypeMismatchException;

  @Nullable
  <T> T convertIfNecessary(@Nullable Object var1, @Nullable Class<T> var2, @Nullable MethodParameter var3) throws TypeMismatchException;

  @Nullable
  <T> T convertIfNecessary(@Nullable Object var1, @Nullable Class<T> var2, @Nullable Field var3) throws TypeMismatchException;
}