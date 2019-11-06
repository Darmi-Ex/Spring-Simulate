package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class AbstractAliasAwareAnnotationAttributeExtractor<S> implements AnnotationAttributeExtractor<S> {
  private final Class<? extends Annotation> annotationType;
  @Nullable
  private final Object annotatedElement;
  private final S source;
  private final Map<String, List<String>> attributeAliasMap;

  AbstractAliasAwareAnnotationAttributeExtractor(Class<? extends Annotation> annotationType, @Nullable Object annotatedElement, S source) {
    Assert.notNull(annotationType, "annotationType must not be null");
    Assert.notNull(source, "source must not be null");
    this.annotationType = annotationType;
    this.annotatedElement = annotatedElement;
    this.source = source;
    this.attributeAliasMap = AnnotationUtils.getAttributeAliasMap(annotationType);
  }

  public final Class<? extends Annotation> getAnnotationType() {
    return this.annotationType;
  }

  @Nullable
  public final Object getAnnotatedElement() {
    return this.annotatedElement;
  }

  public final S getSource() {
    return this.source;
  }

  @Nullable
  public final Object getAttributeValue(Method attributeMethod) {
    String attributeName = attributeMethod.getName();
    Object attributeValue = this.getRawAttributeValue(attributeMethod);
    List<String> aliasNames = (List)this.attributeAliasMap.get(attributeName);
    if (aliasNames != null) {
      Object defaultValue = AnnotationUtils.getDefaultValue(this.annotationType, attributeName);
      Iterator var6 = aliasNames.iterator();

      while(var6.hasNext()) {
        String aliasName = (String)var6.next();
        Object aliasValue = this.getRawAttributeValue(aliasName);
        if (!ObjectUtils.nullSafeEquals(attributeValue, aliasValue) && !ObjectUtils.nullSafeEquals(attributeValue, defaultValue) && !ObjectUtils.nullSafeEquals(aliasValue, defaultValue)) {
          String elementName = this.annotatedElement != null ? this.annotatedElement.toString() : "unknown element";
          throw new AnnotationConfigurationException(String.format("In annotation [%s] declared on %s and synthesized from [%s], attribute '%s' and its alias '%s' are present with values of [%s] and [%s], but only one is permitted.", this.annotationType.getName(), elementName, this.source, attributeName, aliasName, ObjectUtils.nullSafeToString(attributeValue), ObjectUtils.nullSafeToString(aliasValue)));
        }

        if (ObjectUtils.nullSafeEquals(attributeValue, defaultValue)) {
          attributeValue = aliasValue;
        }
      }
    }

    return attributeValue;
  }

  @Nullable
  protected abstract Object getRawAttributeValue(Method var1);

  @Nullable
  protected abstract Object getRawAttributeValue(String var1);
}
