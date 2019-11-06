package org.springframework.springcore.core.annotation;

import org.jetbrains.annotations.Nullable;
import org.springframework.springbean.log.Log;
import org.springframework.springcore.core.BridgeMethodResolver;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ClassUtils;
import org.springframework.springcore.utils.ConcurrentReferenceHashMap;
import org.springframework.springcore.utils.ObjectUtils;
import org.springframework.springcore.utils.ReflectionUtils;
import org.springframework.springcore.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AnnotationUtils {
  public static final String VALUE = "value";
  private static final Map<AnnotationUtils.AnnotationCacheKey, Annotation> findAnnotationCache = new ConcurrentReferenceHashMap(256);
  private static final Map<AnnotationCacheKey, Boolean> metaPresentCache = new ConcurrentReferenceHashMap(256);
  private static final Map<Class<?>, Set<Method>> annotatedBaseTypeCache = new ConcurrentReferenceHashMap(256);
  /** @deprecated */
  @Deprecated
  private static final Map<Class<?>, ?> annotatedInterfaceCache;
  private static final Map<Class<? extends Annotation>, Boolean> synthesizableCache;
  private static final Map<Class<? extends Annotation>, Map<String, List<String>>> attributeAliasesCache;
  private static final Map<Class<? extends Annotation>, List<Method>> attributeMethodsCache;
  private static final Map<Method, AnnotationUtils.AliasDescriptor> aliasDescriptorCache;
  @Nullable
  private static transient Log logger;

  public AnnotationUtils() {
  }

  @Nullable
  public static <A extends Annotation> A getAnnotation(Annotation annotation, Class<A> annotationType) {
    if (annotationType.isInstance(annotation)) {
      return (A) synthesizeAnnotation(annotation);
    } else {
      Class annotatedElement = annotation.annotationType();

      try {
        return (A) synthesizeAnnotation(annotatedElement.getAnnotation(annotationType), (AnnotatedElement)annotatedElement);
      } catch (Throwable var4) {
        handleIntrospectionFailure(annotatedElement, var4);
        return null;
      }
    }
  }

  @Nullable
  public static <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
    try {
      A annotation = annotatedElement.getAnnotation(annotationType);
      if (annotation == null) {
        Annotation[] var3 = annotatedElement.getAnnotations();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
          Annotation metaAnn = var3[var5];
          annotation = metaAnn.annotationType().getAnnotation(annotationType);
          if (annotation != null) {
            break;
          }
        }
      }

      return annotation != null ? (A) synthesizeAnnotation(annotation, annotatedElement) : null;
    } catch (Throwable var7) {
      handleIntrospectionFailure(annotatedElement, var7);
      return null;
    }
  }

  @Nullable
  public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
    Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
    return (A) getAnnotation((AnnotatedElement)resolvedMethod, (Class)annotationType);
  }

  @Nullable
  public static Annotation[] getAnnotations(AnnotatedElement annotatedElement) {
    try {
      return synthesizeAnnotationArray((Annotation[])annotatedElement.getAnnotations(), (Object)annotatedElement);
    } catch (Throwable var2) {
      handleIntrospectionFailure(annotatedElement, var2);
      return null;
    }
  }

  @Nullable
  public static Annotation[] getAnnotations(Method method) {
    try {
      return synthesizeAnnotationArray((Annotation[])BridgeMethodResolver.findBridgedMethod(method).getAnnotations(), (Object)method);
    } catch (Throwable var2) {
      handleIntrospectionFailure(method, var2);
      return null;
    }
  }

  public static <A extends Annotation> Set<A> getRepeatableAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType) {
    return getRepeatableAnnotations(annotatedElement, annotationType, (Class)null);
  }

  public static <A extends Annotation> Set<A> getRepeatableAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType, @Nullable Class<? extends Annotation> containerAnnotationType) {
    Set<A> annotations = getDeclaredRepeatableAnnotations(annotatedElement, annotationType, containerAnnotationType);
    if (!annotations.isEmpty()) {
      return annotations;
    } else {
      if (annotatedElement instanceof Class) {
        Class<?> superclass = ((Class)annotatedElement).getSuperclass();
        if (superclass != null && Object.class != superclass) {
          return getRepeatableAnnotations(superclass, annotationType, containerAnnotationType);
        }
      }

      return getRepeatableAnnotations(annotatedElement, annotationType, containerAnnotationType, false);
    }
  }

  public static <A extends Annotation> Set<A> getDeclaredRepeatableAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType) {
    return getDeclaredRepeatableAnnotations(annotatedElement, annotationType, (Class)null);
  }

  public static <A extends Annotation> Set<A> getDeclaredRepeatableAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType, @Nullable Class<? extends Annotation> containerAnnotationType) {
    return getRepeatableAnnotations(annotatedElement, annotationType, containerAnnotationType, true);
  }

  private static <A extends Annotation> Set<A> getRepeatableAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType, @Nullable Class<? extends Annotation> containerAnnotationType, boolean declaredMode) {
    try {
      if (annotatedElement instanceof Method) {
        annotatedElement = BridgeMethodResolver.findBridgedMethod((Method)annotatedElement);
      }

      return (new AnnotationUtils.AnnotationCollector(annotationType, containerAnnotationType, declaredMode)).getResult((AnnotatedElement)annotatedElement);
    } catch (Throwable var5) {
      handleIntrospectionFailure((AnnotatedElement)annotatedElement, var5);
      return Collections.emptySet();
    }
  }

  @Nullable
  public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
    A ann = (A) findAnnotation((AnnotatedElement)annotatedElement, annotationType, new HashSet());
    return ann != null ? (A) synthesizeAnnotation(ann, annotatedElement) : null;
  }

  @Nullable
  private static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {
    try {
      A annotation = annotatedElement.getDeclaredAnnotation(annotationType);
      if (annotation != null) {
        return annotation;
      }

      Annotation[] var4 = annotatedElement.getDeclaredAnnotations();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
        Annotation declaredAnn = var4[var6];
        Class<? extends Annotation> declaredType = declaredAnn.annotationType();
        if (!isInJavaLangAnnotationPackage(declaredType) && visited.add(declaredAnn)) {
          annotation = findAnnotation((AnnotatedElement)declaredType, annotationType, visited);
          if (annotation != null) {
            return annotation;
          }
        }
      }
    } catch (Throwable var9) {
      handleIntrospectionFailure(annotatedElement, var9);
    }

    return null;
  }

  @Nullable
  public static <A extends Annotation> A findAnnotation(Method method, @Nullable Class<A> annotationType) {
    Assert.notNull(method, "Method must not be null");
    if (annotationType == null) {
      return null;
    } else {
      AnnotationUtils.AnnotationCacheKey cacheKey = new AnnotationUtils.AnnotationCacheKey(method, annotationType);
      A result = (A) findAnnotationCache.get(cacheKey);
      if (result == null) {
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        result = findAnnotation((AnnotatedElement)resolvedMethod, annotationType);
        if (result == null) {
          result = searchOnInterfaces(method, annotationType, method.getDeclaringClass().getInterfaces());
        }

        Class clazz = method.getDeclaringClass();

        while(result == null) {
          clazz = clazz.getSuperclass();
          if (clazz == null || Object.class == clazz) {
            break;
          }

          try {
            Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            Method resolvedEquivalentMethod = BridgeMethodResolver.findBridgedMethod(equivalentMethod);
            result = findAnnotation((AnnotatedElement)resolvedEquivalentMethod, annotationType);
          } catch (NoSuchMethodException var8) {
          }

          if (result == null) {
            result = searchOnInterfaces(method, annotationType, clazz.getInterfaces());
          }
        }

        if (result != null) {
          result = (A) synthesizeAnnotation(result, (AnnotatedElement)method);
          findAnnotationCache.put(cacheKey, result);
        }
      }

      return result;
    }
  }

  @Nullable
  private static <A extends Annotation> A searchOnInterfaces(Method method, Class<A> annotationType, Class... ifcs) {
    Class[] var3 = ifcs;
    int var4 = ifcs.length;

    for(int var5 = 0; var5 < var4; ++var5) {
      Class<?> ifc = var3[var5];
      Set<Method> annotatedMethods = getAnnotatedMethodsInBaseType(ifc);
      if (!annotatedMethods.isEmpty()) {
        Iterator var8 = annotatedMethods.iterator();

        while(var8.hasNext()) {
          Method annotatedMethod = (Method)var8.next();
          if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypes(), method.getParameterTypes())) {
            A annotation = getAnnotation(annotatedMethod, annotationType);
            if (annotation != null) {
              return annotation;
            }
          }
        }
      }
    }

    return null;
  }

  static Set<Method> getAnnotatedMethodsInBaseType(Class<?> baseType) {
    boolean ifcCheck = baseType.isInterface();
    if (ifcCheck && ClassUtils.isJavaLanguageInterface(baseType)) {
      return Collections.emptySet();
    } else {
      Set<Method> annotatedMethods = (Set)annotatedBaseTypeCache.get(baseType);
      if (annotatedMethods != null) {
        return (Set)annotatedMethods;
      } else {
        Method[] methods = ifcCheck ? baseType.getMethods() : baseType.getDeclaredMethods();
        Method[] var4 = methods;
        int var5 = methods.length;

        for(int var6 = 0; var6 < var5; ++var6) {
          Method baseMethod = var4[var6];

          try {
            if ((ifcCheck || !Modifier.isPrivate(baseMethod.getModifiers())) && hasSearchableAnnotations(baseMethod)) {
              if (annotatedMethods == null) {
                annotatedMethods = new HashSet();
              }

              ((Set)annotatedMethods).add(baseMethod);
            }
          } catch (Throwable var9) {
            handleIntrospectionFailure(baseMethod, var9);
          }
        }

        if (annotatedMethods == null) {
          annotatedMethods = Collections.emptySet();
        }

        annotatedBaseTypeCache.put(baseType, annotatedMethods);
        return (Set)annotatedMethods;
      }
    }
  }

  private static boolean hasSearchableAnnotations(Method ifcMethod) {
    Annotation[] anns = ifcMethod.getAnnotations();
    if (anns.length == 0) {
      return false;
    } else if (anns.length != 1) {
      return true;
    } else {
      Class<?> annType = anns[0].annotationType();
      return annType != Nullable.class && annType != Deprecated.class;
    }
  }

  @Nullable
  public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
    return findAnnotation(clazz, annotationType, true);
  }

  @Nullable
  private static <A extends Annotation> A findAnnotation(Class<?> clazz, @Nullable Class<A> annotationType, boolean synthesize) {
    Assert.notNull(clazz, "Class must not be null");
    if (annotationType == null) {
      return null;
    } else {
      AnnotationUtils.AnnotationCacheKey cacheKey = new AnnotationUtils.AnnotationCacheKey(clazz, annotationType);
      A result = (A) findAnnotationCache.get(cacheKey);
      if (result == null) {
        result = (A) findAnnotation((Class)clazz, annotationType, new HashSet());
        if (result != null && synthesize) {
          result = (A) synthesizeAnnotation(result, (AnnotatedElement)clazz);
          findAnnotationCache.put(cacheKey, result);
        }
      }

      return result;
    }
  }

  @Nullable
  private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {
    int var5;
    Annotation annotation;
    try {
      annotation = clazz.getDeclaredAnnotation(annotationType);
      if (annotation != null) {
        return (A) annotation;
      }

      Annotation[] var4 = clazz.getDeclaredAnnotations();
      var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
        annotation = var4[var6];
        Class<? extends Annotation> declaredType = annotation.annotationType();
        if (!isInJavaLangAnnotationPackage(declaredType) && visited.add(annotation)) {
          annotation = findAnnotation(declaredType, annotationType, visited);
          if (annotation != null) {
            return (A) annotation;
          }
        }
      }
    } catch (Throwable var9) {
      handleIntrospectionFailure(clazz, var9);
      return null;
    }

    Class[] var10 = clazz.getInterfaces();
    int var12 = var10.length;

    for(var5 = 0; var5 < var12; ++var5) {
      Class<?> ifc = var10[var5];
      annotation = findAnnotation(ifc, annotationType, visited);
      if (annotation != null) {
        return (A) annotation;
      }
    }

    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null && Object.class != superclass) {
      return findAnnotation(superclass, annotationType, visited);
    } else {
      return null;
    }
  }

  @Nullable
  public static Class<?> findAnnotationDeclaringClass(Class<? extends Annotation> annotationType, @Nullable Class<?> clazz) {
    if (clazz != null && Object.class != clazz) {
      return isAnnotationDeclaredLocally(annotationType, clazz) ? clazz : findAnnotationDeclaringClass(annotationType, clazz.getSuperclass());
    } else {
      return null;
    }
  }

  @Nullable
  public static Class<?> findAnnotationDeclaringClassForTypes(List<Class<? extends Annotation>> annotationTypes, @Nullable Class<?> clazz) {
    if (clazz != null && Object.class != clazz) {
      Iterator var2 = annotationTypes.iterator();

      Class annotationType;
      do {
        if (!var2.hasNext()) {
          return findAnnotationDeclaringClassForTypes(annotationTypes, clazz.getSuperclass());
        }

        annotationType = (Class)var2.next();
      } while(!isAnnotationDeclaredLocally(annotationType, clazz));

      return clazz;
    } else {
      return null;
    }
  }

  public static boolean isAnnotationDeclaredLocally(Class<? extends Annotation> annotationType, Class<?> clazz) {
    try {
      return clazz.getDeclaredAnnotation(annotationType) != null;
    } catch (Throwable var3) {
      handleIntrospectionFailure(clazz, var3);
      return false;
    }
  }

  public static boolean isAnnotationInherited(Class<? extends Annotation> annotationType, Class<?> clazz) {
    return clazz.isAnnotationPresent(annotationType) && !isAnnotationDeclaredLocally(annotationType, clazz);
  }

  public static boolean isAnnotationMetaPresent(Class<? extends Annotation> annotationType, @Nullable Class<? extends Annotation> metaAnnotationType) {
    Assert.notNull(annotationType, "Annotation type must not be null");
    if (metaAnnotationType == null) {
      return false;
    } else {
      AnnotationUtils.AnnotationCacheKey cacheKey = new AnnotationUtils.AnnotationCacheKey(annotationType, metaAnnotationType);
      Boolean metaPresent = (Boolean)metaPresentCache.get(cacheKey);
      if (metaPresent != null) {
        return metaPresent;
      } else {
        metaPresent = Boolean.FALSE;
        if (findAnnotation(annotationType, metaAnnotationType, false) != null) {
          metaPresent = Boolean.TRUE;
        }

        metaPresentCache.put(cacheKey, metaPresent);
        return metaPresent;
      }
    }
  }

  public static boolean isInJavaLangAnnotationPackage(@Nullable Annotation annotation) {
    return annotation != null && isInJavaLangAnnotationPackage(annotation.annotationType());
  }

  static boolean isInJavaLangAnnotationPackage(@Nullable Class<? extends Annotation> annotationType) {
    return annotationType != null && isInJavaLangAnnotationPackage(annotationType.getName());
  }

  public static boolean isInJavaLangAnnotationPackage(@Nullable String annotationType) {
    return annotationType != null && annotationType.startsWith("java.lang.annotation");
  }

  public static void validateAnnotation(Annotation annotation) {
    Iterator var1 = getAttributeMethods(annotation.annotationType()).iterator();

    while(true) {
      Method method;
      Class returnType;
      do {
        if (!var1.hasNext()) {
          return;
        }

        method = (Method)var1.next();
        returnType = method.getReturnType();
      } while(returnType != Class.class && returnType != Class[].class);

      try {
        method.invoke(annotation);
      } catch (Throwable var5) {
        throw new IllegalStateException("Could not obtain annotation attribute value for " + method, var5);
      }
    }
  }

  public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
    return getAnnotationAttributes((AnnotatedElement)null, annotation);
  }

  public static Map<String, Object> getAnnotationAttributes(Annotation annotation, boolean classValuesAsString) {
    return getAnnotationAttributes(annotation, classValuesAsString, false);
  }

  public static AnnotationAttributes getAnnotationAttributes(Annotation annotation, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    return getAnnotationAttributes((AnnotatedElement)null, annotation, classValuesAsString, nestedAnnotationsAsMap);
  }

  public static AnnotationAttributes getAnnotationAttributes(@Nullable AnnotatedElement annotatedElement, Annotation annotation) {
    return getAnnotationAttributes(annotatedElement, annotation, false, false);
  }

  public static AnnotationAttributes getAnnotationAttributes(@Nullable AnnotatedElement annotatedElement, Annotation annotation, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    return getAnnotationAttributes((Object)annotatedElement, annotation, classValuesAsString, nestedAnnotationsAsMap);
  }

  private static AnnotationAttributes getAnnotationAttributes(@Nullable Object annotatedElement, Annotation annotation, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    AnnotationAttributes attributes = retrieveAnnotationAttributes(annotatedElement, annotation, classValuesAsString, nestedAnnotationsAsMap);
    postProcessAnnotationAttributes(annotatedElement, attributes, classValuesAsString, nestedAnnotationsAsMap);
    return attributes;
  }

  static AnnotationAttributes retrieveAnnotationAttributes(@Nullable Object annotatedElement, Annotation annotation, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    Class<? extends Annotation> annotationType = annotation.annotationType();
    AnnotationAttributes attributes = new AnnotationAttributes(annotationType);
    Iterator var6 = getAttributeMethods(annotationType).iterator();

    while(var6.hasNext()) {
      Method method = (Method)var6.next();

      try {
        Object attributeValue = method.invoke(annotation);
        Object defaultValue = method.getDefaultValue();
        if (defaultValue != null && ObjectUtils.nullSafeEquals(attributeValue, defaultValue)) {
          attributeValue = new AnnotationUtils.DefaultValueHolder(defaultValue);
        }

        attributes.put(method.getName(), adaptValue(annotatedElement, attributeValue, classValuesAsString, nestedAnnotationsAsMap));
      } catch (Throwable var10) {
        if (var10 instanceof InvocationTargetException) {
          Throwable targetException = ((InvocationTargetException)var10).getTargetException();
          rethrowAnnotationConfigurationException(targetException);
        }

        throw new IllegalStateException("Could not obtain annotation attribute value for " + method, var10);
      }
    }

    return attributes;
  }

  @Nullable
  static Object adaptValue(@Nullable Object annotatedElement, @Nullable Object value, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    int i;
    if (classValuesAsString) {
      if (value instanceof Class) {
        return ((Class)value).getName();
      }

      if (value instanceof Class[]) {
        Class<?>[] clazzArray = (Class[])((Class[])value);
        String[] classNames = new String[clazzArray.length];

        for(i = 0; i < clazzArray.length; ++i) {
          classNames[i] = clazzArray[i].getName();
        }

        return classNames;
      }
    }

    if (value instanceof Annotation) {
      Annotation annotation = (Annotation)value;
      return nestedAnnotationsAsMap ? getAnnotationAttributes(annotatedElement, annotation, classValuesAsString, true) : synthesizeAnnotation(annotation, annotatedElement);
    } else if (!(value instanceof Annotation[])) {
      return value;
    } else {
      Annotation[] annotations = (Annotation[])((Annotation[])value);
      if (!nestedAnnotationsAsMap) {
        return synthesizeAnnotationArray(annotations, annotatedElement);
      } else {
        AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[annotations.length];

        for(i = 0; i < annotations.length; ++i) {
          mappedAnnotations[i] = getAnnotationAttributes(annotatedElement, annotations[i], classValuesAsString, true);
        }

        return mappedAnnotations;
      }
    }
  }

  public static void registerDefaultValues(AnnotationAttributes attributes) {
    Class<? extends Annotation> annotationType = attributes.annotationType();
    if (annotationType != null && Modifier.isPublic(annotationType.getModifiers())) {
      Iterator var2 = getAttributeMethods(annotationType).iterator();

      while(true) {
        String attributeName;
        Object defaultValue;
        do {
          do {
            if (!var2.hasNext()) {
              return;
            }

            Method annotationAttribute = (Method)var2.next();
            attributeName = annotationAttribute.getName();
            defaultValue = annotationAttribute.getDefaultValue();
          } while(defaultValue == null);
        } while(attributes.containsKey(attributeName));

        if (defaultValue instanceof Annotation) {
          defaultValue = getAnnotationAttributes((Annotation)defaultValue, false, true);
        } else if (defaultValue instanceof Annotation[]) {
          Annotation[] realAnnotations = (Annotation[])((Annotation[])defaultValue);
          AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[realAnnotations.length];

          for(int i = 0; i < realAnnotations.length; ++i) {
            mappedAnnotations[i] = getAnnotationAttributes(realAnnotations[i], false, true);
          }

          defaultValue = mappedAnnotations;
        }

        attributes.put(attributeName, new AnnotationUtils.DefaultValueHolder(defaultValue));
      }
    }
  }

  public static void postProcessAnnotationAttributes(@Nullable Object annotatedElement, AnnotationAttributes attributes, boolean classValuesAsString) {
    postProcessAnnotationAttributes(annotatedElement, attributes, classValuesAsString, false);
  }

  static void postProcessAnnotationAttributes(@Nullable Object annotatedElement, @Nullable AnnotationAttributes attributes, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
    if (attributes != null) {
      Class<? extends Annotation> annotationType = attributes.annotationType();
      Set<String> valuesAlreadyReplaced = new HashSet();
      if (!attributes.validated) {
        Map<String, List<String>> aliasMap = getAttributeAliasMap(annotationType);
        aliasMap.forEach((attributeNamex, aliasedAttributeNames) -> {
          if (!valuesAlreadyReplaced.contains(attributeNamex)) {
            Object value = attributes.get(attributeNamex);
            boolean valuePresent = value != null && !(value instanceof AnnotationUtils.DefaultValueHolder);
            Iterator var9 = aliasedAttributeNames.iterator();

            String aliasedAttributeName;
            Object aliasedValue;
            do {
              while(true) {
                boolean aliasPresent;
                do {
                  do {
                    if (!var9.hasNext()) {
                      return;
                    }

                    aliasedAttributeName = (String)var9.next();
                  } while(valuesAlreadyReplaced.contains(aliasedAttributeName));

                  aliasedValue = attributes.get(aliasedAttributeName);
                  aliasPresent = aliasedValue != null && !(aliasedValue instanceof AnnotationUtils.DefaultValueHolder);
                } while(!valuePresent && !aliasPresent);

                if (valuePresent && aliasPresent) {
                  break;
                }

                if (aliasPresent) {
                  attributes.put(attributeNamex, adaptValue(annotatedElement, aliasedValue, classValuesAsString, nestedAnnotationsAsMap));
                  valuesAlreadyReplaced.add(attributeNamex);
                } else {
                  attributes.put(aliasedAttributeName, adaptValue(annotatedElement, value, classValuesAsString, nestedAnnotationsAsMap));
                  valuesAlreadyReplaced.add(aliasedAttributeName);
                }
              }
            } while(ObjectUtils.nullSafeEquals(value, aliasedValue));

            String elementAsString = annotatedElement != null ? annotatedElement.toString() : "unknown element";
            throw new AnnotationConfigurationException(String.format("In AnnotationAttributes for annotation [%s] declared on %s, attribute '%s' and its alias '%s' are declared with values of [%s] and [%s], but only one is permitted.", attributes.displayName, elementAsString, attributeNamex, aliasedAttributeName, ObjectUtils.nullSafeToString(value), ObjectUtils.nullSafeToString(aliasedValue)));
          }
        });
        attributes.validated = true;
      }

      Iterator var9 = attributes.keySet().iterator();

      while(var9.hasNext()) {
        String attributeName = (String)var9.next();
        if (!valuesAlreadyReplaced.contains(attributeName)) {
          Object value = attributes.get(attributeName);
          if (value instanceof AnnotationUtils.DefaultValueHolder) {
            value = ((AnnotationUtils.DefaultValueHolder)value).defaultValue;
            attributes.put(attributeName, adaptValue(annotatedElement, value, classValuesAsString, nestedAnnotationsAsMap));
          }
        }
      }

    }
  }

  @Nullable
  public static Object getValue(Annotation annotation) {
    return getValue(annotation, "value");
  }

  @Nullable
  public static Object getValue(@Nullable Annotation annotation, @Nullable String attributeName) {
    if (annotation != null && StringUtils.hasText(attributeName)) {
      try {
        Method method = annotation.annotationType().getDeclaredMethod(attributeName);
        ReflectionUtils.makeAccessible(method);
        return method.invoke(annotation);
      } catch (InvocationTargetException var3) {
        rethrowAnnotationConfigurationException(var3.getTargetException());
        throw new IllegalStateException("Could not obtain value for annotation attribute '" + attributeName + "' in " + annotation, var3);
      } catch (Throwable var4) {
        handleIntrospectionFailure(annotation.getClass(), var4);
        return null;
      }
    } else {
      return null;
    }
  }

  @Nullable
  public static Object getDefaultValue(Annotation annotation) {
    return getDefaultValue(annotation, "value");
  }

  @Nullable
  public static Object getDefaultValue(@Nullable Annotation annotation, @Nullable String attributeName) {
    return annotation == null ? null : getDefaultValue(annotation.annotationType(), attributeName);
  }

  @Nullable
  public static Object getDefaultValue(Class<? extends Annotation> annotationType) {
    return getDefaultValue(annotationType, "value");
  }

  @Nullable
  public static Object getDefaultValue(@Nullable Class<? extends Annotation> annotationType, @Nullable String attributeName) {
    if (annotationType != null && StringUtils.hasText(attributeName)) {
      try {
        return annotationType.getDeclaredMethod(attributeName).getDefaultValue();
      } catch (Throwable var3) {
        handleIntrospectionFailure(annotationType, var3);
        return null;
      }
    } else {
      return null;
    }
  }

  static <A extends Annotation> Annotation synthesizeAnnotation(A annotation) {
    return synthesizeAnnotation(annotation, (AnnotatedElement)null);
  }

  public static <A extends Annotation> Annotation synthesizeAnnotation(A annotation, @Nullable AnnotatedElement annotatedElement) {
    return synthesizeAnnotation(annotation, (Object)annotatedElement);
  }

  static <A extends Annotation> Annotation synthesizeAnnotation(A annotation, @Nullable Object annotatedElement) {
    if (annotation instanceof SynthesizedAnnotation) {
      return annotation;
    } else {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (!isSynthesizable(annotationType)) {
        return annotation;
      } else {
        DefaultAnnotationAttributeExtractor attributeExtractor = new DefaultAnnotationAttributeExtractor(annotation, annotatedElement);
        InvocationHandler handler = new SynthesizedAnnotationInvocationHandler(attributeExtractor);
        Class<?>[] exposedInterfaces = new Class[]{annotationType, SynthesizedAnnotation.class};
        return (Annotation) Proxy.newProxyInstance(annotation.getClass().getClassLoader(), exposedInterfaces, handler);
      }
    }
  }

  public static <A extends Annotation> A synthesizeAnnotation(Map<String, Object> attributes, Class<A> annotationType, @Nullable AnnotatedElement annotatedElement) {
    MapAnnotationAttributeExtractor attributeExtractor = new MapAnnotationAttributeExtractor(attributes, annotationType, annotatedElement);
    InvocationHandler handler = new SynthesizedAnnotationInvocationHandler(attributeExtractor);
    Class<?>[] exposedInterfaces = canExposeSynthesizedMarker(annotationType) ? new Class[]{annotationType, SynthesizedAnnotation.class} : new Class[]{annotationType};
    return (A) Proxy.newProxyInstance(annotationType.getClassLoader(), exposedInterfaces, handler);
  }

  public static <A extends Annotation> A synthesizeAnnotation(Class<A> annotationType) {
    return synthesizeAnnotation(Collections.emptyMap(), annotationType, (AnnotatedElement)null);
  }

  static Annotation[] synthesizeAnnotationArray(Annotation[] annotations, @Nullable Object annotatedElement) {
    Annotation[] synthesized = (Annotation[])((Annotation[]) Array.newInstance(annotations.getClass().getComponentType(), annotations.length));

    for(int i = 0; i < annotations.length; ++i) {
      synthesized[i] = synthesizeAnnotation(annotations[i], annotatedElement);
    }

    return synthesized;
  }

  @Nullable
  static <A extends Annotation> A[] synthesizeAnnotationArray(@Nullable Map<String, Object>[] maps, Class<A> annotationType) {
    if (maps == null) {
      return null;
    } else {
      A[] synthesized = (A[]) Array.newInstance(annotationType, maps.length);

      for(int i = 0; i < maps.length; ++i) {
        synthesized[i] = synthesizeAnnotation(maps[i], annotationType, (AnnotatedElement)null);
      }

      return synthesized;
    }
  }

  static Map<String, List<String>> getAttributeAliasMap(@Nullable Class<? extends Annotation> annotationType) {
    if (annotationType == null) {
      return Collections.emptyMap();
    } else {
      Map<String, List<String>> map = (Map)attributeAliasesCache.get(annotationType);
      if (map != null) {
        return map;
      } else {
        map = new LinkedHashMap();
        Iterator var2 = getAttributeMethods(annotationType).iterator();

        while(var2.hasNext()) {
          Method attribute = (Method)var2.next();
          List<String> aliasNames = getAttributeAliasNames(attribute);
          if (!aliasNames.isEmpty()) {
            map.put(attribute.getName(), aliasNames);
          }
        }

        attributeAliasesCache.put(annotationType, map);
        return map;
      }
    }
  }

  private static boolean canExposeSynthesizedMarker(Class<? extends Annotation> annotationType) {
    try {
      return Class.forName(SynthesizedAnnotation.class.getName(), false, annotationType.getClassLoader()) == SynthesizedAnnotation.class;
    } catch (ClassNotFoundException var2) {
      return false;
    }
  }

  private static boolean isSynthesizable(Class<? extends Annotation> annotationType) {
    Boolean synthesizable = (Boolean)synthesizableCache.get(annotationType);
    if (synthesizable != null) {
      return synthesizable;
    } else {
      synthesizable = Boolean.FALSE;
      Iterator var2 = getAttributeMethods(annotationType).iterator();

      while(var2.hasNext()) {
        Method attribute = (Method)var2.next();
        if (!getAttributeAliasNames(attribute).isEmpty()) {
          synthesizable = Boolean.TRUE;
          break;
        }

        Class<?> returnType = attribute.getReturnType();
        if (Annotation[].class.isAssignableFrom(returnType)) {
          Class<? extends Annotation> nestedAnnotationType = (Class<? extends Annotation>) returnType.getComponentType();
          if (isSynthesizable(nestedAnnotationType)) {
            synthesizable = Boolean.TRUE;
            break;
          }
        } else if (Annotation.class.isAssignableFrom(returnType) && isSynthesizable((Class<? extends Annotation>) returnType)) {
          synthesizable = Boolean.TRUE;
          break;
        }
      }

      synthesizableCache.put(annotationType, synthesizable);
      return synthesizable;
    }
  }

  static List<String> getAttributeAliasNames(Method attribute) {
    AnnotationUtils.AliasDescriptor descriptor = AnnotationUtils.AliasDescriptor.from(attribute);
    return descriptor != null ? descriptor.getAttributeAliasNames() : Collections.emptyList();
  }

  @Nullable
  static String getAttributeOverrideName(Method attribute, @Nullable Class<? extends Annotation> metaAnnotationType) {
    AnnotationUtils.AliasDescriptor descriptor = AnnotationUtils.AliasDescriptor.from(attribute);
    return descriptor != null && metaAnnotationType != null ? descriptor.getAttributeOverrideName(metaAnnotationType) : null;
  }

  static List<Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
    List<Method> methods = (List)attributeMethodsCache.get(annotationType);
    if (methods != null) {
      return methods;
    } else {
      methods = new ArrayList();
      Method[] var2 = annotationType.getDeclaredMethods();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
        Method method = var2[var4];
        if (isAttributeMethod(method)) {
          ReflectionUtils.makeAccessible(method);
          methods.add(method);
        }
      }

      attributeMethodsCache.put(annotationType, methods);
      return methods;
    }
  }

  @Nullable
  static Annotation getAnnotation(AnnotatedElement element, String annotationName) {
    Annotation[] var2 = element.getAnnotations();
    int var3 = var2.length;

    for(int var4 = 0; var4 < var3; ++var4) {
      Annotation annotation = var2[var4];
      if (annotation.annotationType().getName().equals(annotationName)) {
        return annotation;
      }
    }

    return null;
  }

  static boolean isAttributeMethod(@Nullable Method method) {
    return method != null && method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE;
  }

  static boolean isAnnotationTypeMethod(@Nullable Method method) {
    return method != null && method.getName().equals("annotationType") && method.getParameterCount() == 0;
  }

  @Nullable
  static Class<? extends Annotation> resolveContainerAnnotationType(Class<? extends Annotation> annotationType) {
    Repeatable repeatable = (Repeatable)getAnnotation((AnnotatedElement)annotationType, (Class)Repeatable.class);
    return repeatable != null ? repeatable.value() : null;
  }

  static void rethrowAnnotationConfigurationException(Throwable ex) {
    if (ex instanceof AnnotationConfigurationException) {
      throw (AnnotationConfigurationException)ex;
    }
  }

  static void handleIntrospectionFailure(@Nullable AnnotatedElement element, Throwable ex) {
    rethrowAnnotationConfigurationException(ex);
    Log loggerToUse = logger;
    if (loggerToUse == null) {
      logger = loggerToUse;
    }

    if (element instanceof Class && Annotation.class.isAssignableFrom((Class)element)) {
      if (loggerToUse.isDebugEnabled()) {
        loggerToUse.debug("Failed to meta-introspect annotation " + element + ": " + ex);
      }
    } else if (loggerToUse.isInfoEnabled()) {
      loggerToUse.info("Failed to introspect annotations on " + element + ": " + ex);
    }

  }

  public static void clearCache() {
    findAnnotationCache.clear();
    metaPresentCache.clear();
    annotatedBaseTypeCache.clear();
    synthesizableCache.clear();
    attributeAliasesCache.clear();
    attributeMethodsCache.clear();
    aliasDescriptorCache.clear();
  }

  static {
    annotatedInterfaceCache = annotatedBaseTypeCache;
    synthesizableCache = new ConcurrentReferenceHashMap(256);
    attributeAliasesCache = new ConcurrentReferenceHashMap(256);
    attributeMethodsCache = new ConcurrentReferenceHashMap(256);
    aliasDescriptorCache = new ConcurrentReferenceHashMap(256);
  }

  private static class DefaultValueHolder {
    final Object defaultValue;

    public DefaultValueHolder(Object defaultValue) {
      this.defaultValue = defaultValue;
    }
  }

  private static class AliasDescriptor {
    private final Method sourceAttribute;
    private final Class<? extends Annotation> sourceAnnotationType;
    private final String sourceAttributeName;
    private final Method aliasedAttribute;
    private final Class<? extends Annotation> aliasedAnnotationType;
    private final String aliasedAttributeName;
    private final boolean isAliasPair;

    @Nullable
    public static AnnotationUtils.AliasDescriptor from(Method attribute) {
      AnnotationUtils.AliasDescriptor descriptor = (AnnotationUtils.AliasDescriptor)AnnotationUtils.aliasDescriptorCache.get(attribute);
      if (descriptor != null) {
        return descriptor;
      } else {
        AliasFor aliasFor = (AliasFor)attribute.getAnnotation(AliasFor.class);
        if (aliasFor == null) {
          return null;
        } else {
          descriptor = new AnnotationUtils.AliasDescriptor(attribute, aliasFor);
          descriptor.validate();
          AnnotationUtils.aliasDescriptorCache.put(attribute, descriptor);
          return descriptor;
        }
      }
    }

    private AliasDescriptor(Method sourceAttribute, AliasFor aliasFor) {
      Class<?> declaringClass = sourceAttribute.getDeclaringClass();
      this.sourceAttribute = sourceAttribute;
      this.sourceAnnotationType = (Class<? extends Annotation>) declaringClass;
      this.sourceAttributeName = sourceAttribute.getName();
      this.aliasedAnnotationType = Annotation.class == aliasFor.annotation() ? this.sourceAnnotationType : aliasFor.annotation();
      this.aliasedAttributeName = this.getAliasedAttributeName(aliasFor, sourceAttribute);
      if (this.aliasedAnnotationType == this.sourceAnnotationType && this.aliasedAttributeName.equals(this.sourceAttributeName)) {
        String msg = String.format("@AliasFor declaration on attribute '%s' in annotation [%s] points to itself. Specify 'annotation' to point to a same-named attribute on a meta-annotation.", sourceAttribute.getName(), declaringClass.getName());
        throw new AnnotationConfigurationException(msg);
      } else {
        try {
          this.aliasedAttribute = this.aliasedAnnotationType.getDeclaredMethod(this.aliasedAttributeName);
        } catch (NoSuchMethodException var6) {
          String msg = String.format("Attribute '%s' in annotation [%s] is declared as an @AliasFor nonexistent attribute '%s' in annotation [%s].", this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName, this.aliasedAnnotationType.getName());
          throw new AnnotationConfigurationException(msg, var6);
        }

        this.isAliasPair = this.sourceAnnotationType == this.aliasedAnnotationType;
      }
    }

    private void validate() {
      if (!this.isAliasPair && !AnnotationUtils.isAnnotationMetaPresent(this.sourceAnnotationType, this.aliasedAnnotationType)) {
        String msg = String.format("@AliasFor declaration on attribute '%s' in annotation [%s] declares an alias for attribute '%s' in meta-annotation [%s] which is not meta-present.", this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName, this.aliasedAnnotationType.getName());
        throw new AnnotationConfigurationException(msg);
      } else {
        String msg;
        if (this.isAliasPair) {
          AliasFor mirrorAliasFor = (AliasFor)this.aliasedAttribute.getAnnotation(AliasFor.class);
          String mirrorAliasedAttributeName;
          if (mirrorAliasFor == null) {
            mirrorAliasedAttributeName = String.format("Attribute '%s' in annotation [%s] must be declared as an @AliasFor [%s].", this.aliasedAttributeName, this.sourceAnnotationType.getName(), this.sourceAttributeName);
            throw new AnnotationConfigurationException(mirrorAliasedAttributeName);
          }

          mirrorAliasedAttributeName = this.getAliasedAttributeName(mirrorAliasFor, this.aliasedAttribute);
          if (!this.sourceAttributeName.equals(mirrorAliasedAttributeName)) {
            msg = String.format("Attribute '%s' in annotation [%s] must be declared as an @AliasFor [%s], not [%s].", this.aliasedAttributeName, this.sourceAnnotationType.getName(), this.sourceAttributeName, mirrorAliasedAttributeName);
            throw new AnnotationConfigurationException(msg);
          }
        }

        Class<?> returnType = this.sourceAttribute.getReturnType();
        Class<?> aliasedReturnType = this.aliasedAttribute.getReturnType();
        if (returnType != aliasedReturnType && (!aliasedReturnType.isArray() || returnType != aliasedReturnType.getComponentType())) {
          msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] and attribute '%s' in annotation [%s] must declare the same return type.", this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName, this.aliasedAnnotationType.getName());
          throw new AnnotationConfigurationException(msg);
        } else {
          if (this.isAliasPair) {
            this.validateDefaultValueConfiguration(this.aliasedAttribute);
          }

        }
      }
    }

    private void validateDefaultValueConfiguration(Method aliasedAttribute) {
      Object defaultValue = this.sourceAttribute.getDefaultValue();
      Object aliasedDefaultValue = aliasedAttribute.getDefaultValue();
      String msg;
      if (defaultValue != null && aliasedDefaultValue != null) {
        if (!ObjectUtils.nullSafeEquals(defaultValue, aliasedDefaultValue)) {
          msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] and attribute '%s' in annotation [%s] must declare the same default value.", this.sourceAttributeName, this.sourceAnnotationType.getName(), aliasedAttribute.getName(), aliasedAttribute.getDeclaringClass().getName());
          throw new AnnotationConfigurationException(msg);
        }
      } else {
        msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] and attribute '%s' in annotation [%s] must declare default values.", this.sourceAttributeName, this.sourceAnnotationType.getName(), aliasedAttribute.getName(), aliasedAttribute.getDeclaringClass().getName());
        throw new AnnotationConfigurationException(msg);
      }
    }

    private void validateAgainst(AnnotationUtils.AliasDescriptor otherDescriptor) {
      this.validateDefaultValueConfiguration(otherDescriptor.sourceAttribute);
    }

    private boolean isOverrideFor(Class<? extends Annotation> metaAnnotationType) {
      return this.aliasedAnnotationType == metaAnnotationType;
    }

    private boolean isAliasFor(AnnotationUtils.AliasDescriptor otherDescriptor) {
      for(AnnotationUtils.AliasDescriptor lhs = this; lhs != null; lhs = lhs.getAttributeOverrideDescriptor()) {
        for(AnnotationUtils.AliasDescriptor rhs = otherDescriptor; rhs != null; rhs = rhs.getAttributeOverrideDescriptor()) {
          if (lhs.aliasedAttribute.equals(rhs.aliasedAttribute)) {
            return true;
          }
        }
      }

      return false;
    }

    public List<String> getAttributeAliasNames() {
      if (this.isAliasPair) {
        return Collections.singletonList(this.aliasedAttributeName);
      } else {
        List<String> aliases = new ArrayList();
        Iterator var2 = this.getOtherDescriptors().iterator();

        while(var2.hasNext()) {
          AnnotationUtils.AliasDescriptor otherDescriptor = (AnnotationUtils.AliasDescriptor)var2.next();
          if (this.isAliasFor(otherDescriptor)) {
            this.validateAgainst(otherDescriptor);
            aliases.add(otherDescriptor.sourceAttributeName);
          }
        }

        return aliases;
      }
    }

    private List<AnnotationUtils.AliasDescriptor> getOtherDescriptors() {
      List<AnnotationUtils.AliasDescriptor> otherDescriptors = new ArrayList();
      Iterator var2 = AnnotationUtils.getAttributeMethods(this.sourceAnnotationType).iterator();

      while(var2.hasNext()) {
        Method currentAttribute = (Method)var2.next();
        if (!this.sourceAttribute.equals(currentAttribute)) {
          AnnotationUtils.AliasDescriptor otherDescriptor = from(currentAttribute);
          if (otherDescriptor != null) {
            otherDescriptors.add(otherDescriptor);
          }
        }
      }

      return otherDescriptors;
    }

    @Nullable
    public String getAttributeOverrideName(Class<? extends Annotation> metaAnnotationType) {
      for(AnnotationUtils.AliasDescriptor desc = this; desc != null; desc = desc.getAttributeOverrideDescriptor()) {
        if (desc.isOverrideFor(metaAnnotationType)) {
          return desc.aliasedAttributeName;
        }
      }

      return null;
    }

    @Nullable
    private AnnotationUtils.AliasDescriptor getAttributeOverrideDescriptor() {
      return this.isAliasPair ? null : from(this.aliasedAttribute);
    }

    private String getAliasedAttributeName(AliasFor aliasFor, Method attribute) {
      String attributeName = aliasFor.attribute();
      String value = aliasFor.value();
      boolean attributeDeclared = StringUtils.hasText(attributeName);
      boolean valueDeclared = StringUtils.hasText(value);
      if (attributeDeclared && valueDeclared) {
        String msg = String.format("In @AliasFor declared on attribute '%s' in annotation [%s], attribute 'attribute' and its alias 'value' are present with values of [%s] and [%s], but only one is permitted.", attribute.getName(), attribute.getDeclaringClass().getName(), attributeName, value);
        throw new AnnotationConfigurationException(msg);
      } else {
        attributeName = attributeDeclared ? attributeName : value;
        return StringUtils.hasText(attributeName) ? attributeName.trim() : attribute.getName();
      }
    }

    public String toString() {
      return String.format("%s: @%s(%s) is an alias for @%s(%s)", this.getClass().getSimpleName(), this.sourceAnnotationType.getSimpleName(), this.sourceAttributeName, this.aliasedAnnotationType.getSimpleName(), this.aliasedAttributeName);
    }
  }

  private static class AnnotationCollector<A extends Annotation> {
    private final Class<A> annotationType;
    @Nullable
    private final Class<? extends Annotation> containerAnnotationType;
    private final boolean declaredMode;
    private final Set<AnnotatedElement> visited = new HashSet();
    private final Set<A> result = new LinkedHashSet();

    AnnotationCollector(Class<A> annotationType, @Nullable Class<? extends Annotation> containerAnnotationType, boolean declaredMode) {
      this.annotationType = annotationType;
      this.containerAnnotationType = containerAnnotationType != null ? containerAnnotationType : AnnotationUtils.resolveContainerAnnotationType(annotationType);
      this.declaredMode = declaredMode;
    }

    Set<A> getResult(AnnotatedElement element) {
      this.process(element);
      return Collections.unmodifiableSet(this.result);
    }

    private void process(AnnotatedElement element) {
      if (this.visited.add(element)) {
        try {
          Annotation[] annotations = this.declaredMode ? element.getDeclaredAnnotations() : element.getAnnotations();
          Annotation[] var3 = annotations;
          int var4 = annotations.length;

          for(int var5 = 0; var5 < var4; ++var5) {
            Annotation ann = var3[var5];
            Class<? extends Annotation> currentAnnotationType = ann.annotationType();
            if (ObjectUtils.nullSafeEquals(this.annotationType, currentAnnotationType)) {
              this.result.add((A) AnnotationUtils.synthesizeAnnotation(ann, element));
            } else if (ObjectUtils.nullSafeEquals(this.containerAnnotationType, currentAnnotationType)) {
              this.result.addAll(this.getValue(element, ann));
            } else if (!AnnotationUtils.isInJavaLangAnnotationPackage(currentAnnotationType)) {
              this.process(currentAnnotationType);
            }
          }
        } catch (Throwable var8) {
          AnnotationUtils.handleIntrospectionFailure(element, var8);
        }
      }

    }

    private List<A> getValue(AnnotatedElement element, Annotation annotation) {
      try {
        List<A> synthesizedAnnotations = new ArrayList();
        A[] value = (A[]) AnnotationUtils.getValue(annotation);
        if (value != null) {
          Annotation[] var5 = value;
          int var6 = value.length;

          for(int var7 = 0; var7 < var6; ++var7) {
            A anno = (A) var5[var7];
            synthesizedAnnotations.add((A) AnnotationUtils.synthesizeAnnotation(anno, element));
          }
        }

        return synthesizedAnnotations;
      } catch (Throwable var9) {
        AnnotationUtils.handleIntrospectionFailure(element, var9);
        return Collections.emptyList();
      }
    }
  }

  private static final class AnnotationCacheKey implements Comparable<AnnotationUtils.AnnotationCacheKey> {
    private final AnnotatedElement element;
    private final Class<? extends Annotation> annotationType;

    public AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
      this.element = element;
      this.annotationType = annotationType;
    }

    public boolean equals(Object other) {
      if (this == other) {
        return true;
      } else if (!(other instanceof AnnotationUtils.AnnotationCacheKey)) {
        return false;
      } else {
        AnnotationUtils.AnnotationCacheKey otherKey = (AnnotationUtils.AnnotationCacheKey)other;
        return this.element.equals(otherKey.element) && this.annotationType.equals(otherKey.annotationType);
      }
    }

    public int hashCode() {
      return this.element.hashCode() * 29 + this.annotationType.hashCode();
    }

    public String toString() {
      return "@" + this.annotationType + " on " + this.element;
    }

    public int compareTo(AnnotationUtils.AnnotationCacheKey other) {
      int result = this.element.toString().compareTo(other.element.toString());
      if (result == 0) {
        result = this.annotationType.getName().compareTo(other.annotationType.getName());
      }

      return result;
    }
  }
}
