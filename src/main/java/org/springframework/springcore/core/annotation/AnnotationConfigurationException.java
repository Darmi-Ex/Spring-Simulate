package org.springframework.springcore.core.annotation;

import org.springframework.springcore.core.NestedRuntimeException;

public class AnnotationConfigurationException extends NestedRuntimeException {
  public AnnotationConfigurationException(String message) {
    super(message);
  }

  public AnnotationConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}