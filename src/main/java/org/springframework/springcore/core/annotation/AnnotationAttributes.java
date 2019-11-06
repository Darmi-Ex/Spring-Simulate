package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AnnotationAttributes extends LinkedHashMap<String, Object> {
  private static final String UNKNOWN = "unknown";
  @Nullable
  private final Class<? extends Annotation> annotationType;
  final String displayName;
  boolean validated = false;

  public AnnotationAttributes() {
    this.annotationType = null;
    this.displayName = "unknown";
  }

  public AnnotationAttributes(int initialCapacity) {
    super(initialCapacity);
    this.annotationType = null;
    this.displayName = "unknown";
  }

  public AnnotationAttributes(Map<String, Object> map) {
    super(map);
    this.annotationType = null;
    this.displayName = "unknown";
  }

  public AnnotationAttributes(AnnotationAttributes other) {
    super(other);
    this.annotationType = other.annotationType;
    this.displayName = other.displayName;
    this.validated = other.validated;
  }

  public AnnotationAttributes(Class<? extends Annotation> annotationType) {
    Assert.notNull(annotationType, "'annotationType' must not be null");
    this.annotationType = annotationType;
    this.displayName = annotationType.getName();
  }

  public AnnotationAttributes(String annotationType, @Nullable ClassLoader classLoader) {
    Assert.notNull(annotationType, "'annotationType' must not be null");
    this.annotationType = (Class<? extends Annotation>) getAnnotationType(annotationType, classLoader);
    this.displayName = annotationType;
  }

  private static Class<?> getAnnotationType(String annotationType, @Nullable ClassLoader classLoader) {
    if (classLoader != null) {
      try {
        return classLoader.loadClass(annotationType);
      } catch (ClassNotFoundException var3) {
      }
    }

    return null;
  }

  @Nullable
  public Class<? extends Annotation> annotationType() {
    return this.annotationType;
  }

  public String getString(String attributeName) {
    return (String)this.getRequiredAttribute(attributeName, String.class);
  }

  public String[] getStringArray(String attributeName) {
    return (String[])this.getRequiredAttribute(attributeName, String[].class);
  }

  public boolean getBoolean(String attributeName) {
    return (Boolean)this.getRequiredAttribute(attributeName, Boolean.class);
  }

  public <N extends Number> Number getNumber(String attributeName) {
    return (Number)this.getRequiredAttribute(attributeName, Number.class);
  }

  public <E extends Enum<?>> Enum getEnum(String attributeName) {
    return (Enum)this.getRequiredAttribute(attributeName, Enum.class);
  }

  public <T> Class<? extends T> getClass(String attributeName) {
    return (Class)this.getRequiredAttribute(attributeName, Class.class);
  }

  public Class<?>[] getClassArray(String attributeName) {
    return (Class[])this.getRequiredAttribute(attributeName, Class[].class);
  }

  public AnnotationAttributes getAnnotation(String attributeName) {
    return (AnnotationAttributes)this.getRequiredAttribute(attributeName, AnnotationAttributes.class);
  }

  public <A extends Annotation> A getAnnotation(String attributeName, Class<A> annotationType) {
    return (A) this.getRequiredAttribute(attributeName, annotationType);
  }

  public AnnotationAttributes[] getAnnotationArray(String attributeName) {
    return (AnnotationAttributes[])this.getRequiredAttribute(attributeName, AnnotationAttributes[].class);
  }

  public <A extends Annotation> Annotation[] getAnnotationArray(String attributeName, Class<A> annotationType) {
    Object array = Array.newInstance(annotationType, 0);
    return (Annotation[])((Annotation[])this.getRequiredAttribute(attributeName, array.getClass()));
  }

  private <T> Object getRequiredAttribute(String attributeName, Class<T> expectedType) {
    Assert.hasText(attributeName, "'attributeName' must not be null or empty");
    Object value = this.get(attributeName);
    this.assertAttributePresence(attributeName, value);
    this.assertNotException(attributeName, value);
    if (!expectedType.isInstance(value) && expectedType.isArray() && expectedType.getComponentType().isInstance(value)) {
      Object array = Array.newInstance(expectedType.getComponentType(), 1);
      Array.set(array, 0, value);
      value = array;
    }

    this.assertAttributeType(attributeName, value, expectedType);
    return value;
  }

  private void assertAttributePresence(String attributeName, Object attributeValue) {
    Assert.notNull(attributeValue, () -> {
      return String.format("Attribute '%s' not found in attributes for annotation [%s]", attributeName, this.displayName);
    });
  }

  private void assertNotException(String attributeName, Object attributeValue) {
    if (attributeValue instanceof Exception) {
      throw new IllegalArgumentException(String.format("Attribute '%s' for annotation [%s] was not resolvable due to exception [%s]", attributeName, this.displayName, attributeValue), (Exception)attributeValue);
    }
  }

  private void assertAttributeType(String attributeName, Object attributeValue, Class<?> expectedType) {
    if (!expectedType.isInstance(attributeValue)) {
      throw new IllegalArgumentException(String.format("Attribute '%s' is of type [%s], but [%s] was expected in attributes for annotation [%s]", attributeName, attributeValue.getClass().getSimpleName(), expectedType.getSimpleName(), this.displayName));
    }
  }

  public Object putIfAbsent(String key, Object value) {
    Object obj = this.get(key);
    if (obj == null) {
      obj = this.put(key, value);
    }

    return obj;
  }

  public String toString() {
    Iterator<Entry<String, Object>> entries = this.entrySet().iterator();
    StringBuilder sb = new StringBuilder("{");

    while(entries.hasNext()) {
      Entry<String, Object> entry = (Entry)entries.next();
      sb.append((String)entry.getKey());
      sb.append('=');
      sb.append(this.valueToString(entry.getValue()));
      sb.append(entries.hasNext() ? ", " : "");
    }

    sb.append("}");
    return sb.toString();
  }

  private String valueToString(Object value) {
    if (value == this) {
      return "(this Map)";
    } else {
      return value instanceof Object[] ? "[" + StringUtils.arrayToDelimitedString((Object[])((Object[])value), ", ") + "]" : String.valueOf(value);
    }
  }

  @Nullable
  public static AnnotationAttributes fromMap(@Nullable Map<String, Object> map) {
    if (map == null) {
      return null;
    } else {
      return map instanceof AnnotationAttributes ? (AnnotationAttributes)map : new AnnotationAttributes(map);
    }
  }
}
