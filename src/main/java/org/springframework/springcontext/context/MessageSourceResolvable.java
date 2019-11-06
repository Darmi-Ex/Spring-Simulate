package org.springframework.springcontext.context;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageSourceResolvable {
  @Nullable
  String[] getCodes();

  @Nullable
  default Object[] getArguments() {
    return null;
  }

  @Nullable
  default String getDefaultMessage() {
    return null;
  }
}
