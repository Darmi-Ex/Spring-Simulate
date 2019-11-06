package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MapAnnotationAttributeExtractor extends AbstractAliasAwareAnnotationAttributeExtractor<Map<String, Object>> {
  MapAnnotationAttributeExtractor(Map<String, Object> attributes, Class<? extends Annotation> annotationType, @Nullable AnnotatedElement annotatedElement) {
    super(annotationType, annotatedElement, enrichAndValidateAttributes(attributes, annotationType));
  }

  @Nullable
  protected Object getRawAttributeValue(Method attributeMethod) {
    return this.getRawAttributeValue(attributeMethod.getName());
  }

  @Nullable
  protected Object getRawAttributeValue(String attributeName) {
    return ((Map)this.getSource()).get(attributeName);
  }

  private static Map<String, Object> enrichAndValidateAttributes(Map<String, Object> originalAttributes, Class<? extends Annotation> annotationType) {
    Map<String, Object> attributes = new LinkedHashMap(originalAttributes);
    Map<String, List<String>> attributeAliasMap = AnnotationUtils.getAttributeAliasMap(annotationType);
    Iterator var4 = AnnotationUtils.getAttributeMethods(annotationType).iterator();

    while(true) {
      String attributeName;
      Object attributeValue;
      Object array;
      Class requiredReturnType;
      Class actualReturnType;
      do {
        if (!var4.hasNext()) {
          return attributes;
        }

        Method attributeMethod = (Method)var4.next();
        attributeName = attributeMethod.getName();
        attributeValue = attributes.get(attributeName);
        if (attributeValue == null) {
          List<String> aliasNames = (List)attributeAliasMap.get(attributeName);
          if (aliasNames != null) {
            Iterator var9 = aliasNames.iterator();

            while(var9.hasNext()) {
              String aliasName = (String)var9.next();
              array = attributes.get(aliasName);
              if (array != null) {
                attributeValue = array;
                attributes.put(attributeName, array);
                break;
              }
            }
          }
        }

        if (attributeValue == null) {
          Object defaultValue = AnnotationUtils.getDefaultValue(annotationType, attributeName);
          if (defaultValue != null) {
            attributeValue = defaultValue;
            attributes.put(attributeName, defaultValue);
          }
        }

        String finalAttributeName = attributeName;
        Assert.notNull(attributeValue, () -> {
          return String.format("Attributes map %s returned null for required attribute '%s' defined by annotation type [%s].", attributes, finalAttributeName, annotationType.getName());
        });
        requiredReturnType = attributeMethod.getReturnType();
        actualReturnType = attributeValue.getClass();
      } while(ClassUtils.isAssignable(requiredReturnType, actualReturnType));

      boolean converted = false;
      if (requiredReturnType.isArray() && requiredReturnType.getComponentType() == actualReturnType) {
        array = Array.newInstance(requiredReturnType.getComponentType(), 1);
        Array.set(array, 0, attributeValue);
        attributes.put(attributeName, array);
        converted = true;
      } else if (Annotation.class.isAssignableFrom(requiredReturnType) && Map.class.isAssignableFrom(actualReturnType)) {
        Map<String, Object> map = (Map)attributeValue;
        attributes.put(attributeName, AnnotationUtils.synthesizeAnnotation(map, requiredReturnType, (AnnotatedElement)null));
        converted = true;
      } else if (requiredReturnType.isArray() && actualReturnType.isArray() && Annotation.class.isAssignableFrom(requiredReturnType.getComponentType()) && Map.class.isAssignableFrom(actualReturnType.getComponentType())) {
        Class<? extends Annotation> nestedAnnotationType = requiredReturnType.getComponentType();
        Map<String, Object>[] maps = (Map[])((Map[])attributeValue);
        attributes.put(attributeName, AnnotationUtils.synthesizeAnnotationArray(maps, nestedAnnotationType));
        converted = true;
      }

      String finalAttributeName1 = attributeName;
      Class finalRequiredReturnType = requiredReturnType;
      Class finalActualReturnType = actualReturnType;
      Assert.isTrue(converted, () -> {
        return String.format("Attributes map %s returned a value of type [%s] for attribute '%s', but a value of type [%s] is required as defined by annotation type [%s].", attributes, finalActualReturnType.getName(), finalAttributeName1, finalRequiredReturnType.getName(), annotationType.getName());
      });
    }
  }
}
