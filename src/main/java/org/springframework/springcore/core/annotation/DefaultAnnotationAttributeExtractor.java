package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

class DefaultAnnotationAttributeExtractor extends AbstractAliasAwareAnnotationAttributeExtractor<Annotation> {
  DefaultAnnotationAttributeExtractor(Annotation annotation, @Nullable Object annotatedElement) {
    super(annotation.annotationType(), annotatedElement, annotation);
  }

  @Nullable
  protected Object getRawAttributeValue(Method attributeMethod) {
    ReflectionUtils.makeAccessible(attributeMethod);
    return ReflectionUtils.invokeMethod(attributeMethod, this.getSource());
  }

  @Nullable
  protected Object getRawAttributeValue(String attributeName) {
    Method attributeMethod = ReflectionUtils.findMethod(this.getAnnotationType(), attributeName);
    return attributeMethod != null ? this.getRawAttributeValue(attributeMethod) : null;
  }
}
