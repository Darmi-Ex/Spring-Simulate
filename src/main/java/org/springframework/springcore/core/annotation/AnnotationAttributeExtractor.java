package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

interface AnnotationAttributeExtractor<S> {
  Class<? extends Annotation> getAnnotationType();

  @Nullable
  Object getAnnotatedElement();

  S getSource();

  @Nullable
  Object getAttributeValue(Method var1);
}