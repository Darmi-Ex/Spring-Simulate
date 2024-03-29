package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ConcurrentReferenceHashMap;
import org.springframework.springcore.utils.ObjectUtils;
import org.springframework.springcore.utils.ReflectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

abstract class SerializableTypeWrapper {
    private static final Class<?>[] SUPPORTED_SERIALIZABLE_TYPES = new Class[]{GenericArrayType.class, ParameterizedType.class, TypeVariable.class, WildcardType.class};
    static final ConcurrentReferenceHashMap<Type, Type> cache = new ConcurrentReferenceHashMap(256);

    SerializableTypeWrapper() {
    }

    @Nullable
    public static Type forField(Field field) {
        Assert.notNull(field, "Field must not be null");
        return forTypeProvider(new SerializableTypeWrapper.FieldTypeProvider(field));
    }

    @Nullable
    public static Type forMethodParameter(MethodParameter methodParameter) {
        return forTypeProvider(new SerializableTypeWrapper.MethodParameterTypeProvider(methodParameter));
    }

    @Nullable
    public static Type forGenericSuperclass(Class<?> type) {
        type.getClass();
        return forTypeProvider(type::getGenericSuperclass);
    }

    public static Type[] forGenericInterfaces(Class<?> type) {
        Type[] result = new Type[type.getGenericInterfaces().length];

        for(int i = 0; i < result.length; ++i) {
            int finalI = i;
            result[i] = forTypeProvider(() -> {
                return type.getGenericInterfaces()[finalI];
            });
        }

        return result;
    }

    public static Type[] forTypeParameters(Class<?> type) {
        Type[] result = new Type[type.getTypeParameters().length];

        for(int i = 0; i < result.length; ++i) {
            int finalI = i;
            result[i] = forTypeProvider(() -> {
                return type.getTypeParameters()[finalI];
            });
        }

        return result;
    }

    public static <T extends Type> T unwrap(T type) {
        Type unwrapped;
        for(unwrapped = type; unwrapped instanceof SerializableTypeWrapper.SerializableTypeProxy; unwrapped = ((SerializableTypeWrapper.SerializableTypeProxy)type).getTypeProvider().getType()) {
        }

        return unwrapped != null ? (T) unwrapped : type;
    }

    @Nullable
    static Type forTypeProvider(SerializableTypeWrapper.TypeProvider provider) {
        Assert.notNull(provider, "Provider must not be null");
        Type providedType = provider.getType();
        if (providedType == null) {
            return null;
        } else if (providedType instanceof Serializable) {
            return providedType;
        } else {
            Type cached = (Type)cache.get(providedType);
            if (cached != null) {
                return cached;
            } else {
                Class[] var3 = SUPPORTED_SERIALIZABLE_TYPES;
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    Class<?> type = var3[var5];
                    if (type.isAssignableFrom(providedType.getClass())) {
                        ClassLoader classLoader = provider.getClass().getClassLoader();
                        Class<?>[] interfaces = new Class[]{type, SerializableTypeWrapper.SerializableTypeProxy.class, Serializable.class};
                        InvocationHandler handler = new SerializableTypeWrapper.TypeProxyInvocationHandler(provider);
                        cached = (Type) Proxy.newProxyInstance(classLoader, interfaces, handler);
                        cache.put(providedType, cached);
                        return cached;
                    }
                }

                throw new IllegalArgumentException("Unsupported Type class: " + providedType.getClass().getName());
            }
        }
    }

    static class MethodInvokeTypeProvider implements SerializableTypeWrapper.TypeProvider {
        private final SerializableTypeWrapper.TypeProvider provider;
        private final String methodName;
        private final Class<?> declaringClass;
        private final int index;
        private transient Method method;
        @Nullable
        private transient volatile Object result;

        public MethodInvokeTypeProvider(SerializableTypeWrapper.TypeProvider provider, Method method, int index) {
            this.provider = provider;
            this.methodName = method.getName();
            this.declaringClass = method.getDeclaringClass();
            this.index = index;
            this.method = method;
        }

        @Nullable
        public Type getType() {
            Object result = this.result;
            if (result == null) {
                result = ReflectionUtils.invokeMethod(this.method, this.provider.getType());
                this.result = result;
            }

            return result instanceof Type[] ? ((Type[])((Type[])result))[this.index] : (Type)result;
        }

        @Nullable
        public Object getSource() {
            return null;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            Method method = ReflectionUtils.findMethod(this.declaringClass, this.methodName);
            if (method == null) {
                throw new IllegalStateException("Cannot find method on deserialization: " + this.methodName);
            } else if (method.getReturnType() != Type.class && method.getReturnType() != Type[].class) {
                throw new IllegalStateException("Invalid return type on deserialized method - needs to be Type or Type[]: " + method);
            } else {
                this.method = method;
            }
        }
    }

    static class MethodParameterTypeProvider implements SerializableTypeWrapper.TypeProvider {
        @Nullable
        private final String methodName;
        private final Class<?>[] parameterTypes;
        private final Class<?> declaringClass;
        private final int parameterIndex;
        private transient MethodParameter methodParameter;

        public MethodParameterTypeProvider(MethodParameter methodParameter) {
            this.methodName = methodParameter.getMethod() != null ? methodParameter.getMethod().getName() : null;
            this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
            this.declaringClass = methodParameter.getDeclaringClass();
            this.parameterIndex = methodParameter.getParameterIndex();
            this.methodParameter = methodParameter;
        }

        public Type getType() {
            return this.methodParameter.getGenericParameterType();
        }

        public Object getSource() {
            return this.methodParameter;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();

            try {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                } else {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }

            } catch (Throwable var3) {
                throw new IllegalStateException("Could not find original class structure", var3);
            }
        }
    }

    static class FieldTypeProvider implements SerializableTypeWrapper.TypeProvider {
        private final String fieldName;
        private final Class<?> declaringClass;
        private transient Field field;

        public FieldTypeProvider(Field field) {
            this.fieldName = field.getName();
            this.declaringClass = field.getDeclaringClass();
            this.field = field;
        }

        public Type getType() {
            return this.field.getGenericType();
        }

        public Object getSource() {
            return this.field;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();

            try {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } catch (Throwable var3) {
                throw new IllegalStateException("Could not find original class structure", var3);
            }
        }
    }

    private static class TypeProxyInvocationHandler implements InvocationHandler, Serializable {
        private final SerializableTypeWrapper.TypeProvider provider;

        public TypeProxyInvocationHandler(SerializableTypeWrapper.TypeProvider provider) {
            this.provider = provider;
        }

        @Nullable
        public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
            if (method.getName().equals("equals") && args != null) {
                Object other = args[0];
                if (other instanceof Type) {
                    other = SerializableTypeWrapper.unwrap((Type)other);
                }

                return ObjectUtils.nullSafeEquals(this.provider.getType(), other);
            } else if (method.getName().equals("hashCode")) {
                return ObjectUtils.nullSafeHashCode(this.provider.getType());
            } else if (method.getName().equals("getTypeProvider")) {
                return this.provider;
            } else if (Type.class == method.getReturnType() && args == null) {
                return SerializableTypeWrapper.forTypeProvider(new SerializableTypeWrapper.MethodInvokeTypeProvider(this.provider, method, -1));
            } else if (Type[].class == method.getReturnType() && args == null) {
                Type[] result = new Type[((Type[])((Type[])method.invoke(this.provider.getType()))).length];

                for(int i = 0; i < result.length; ++i) {
                    result[i] = SerializableTypeWrapper.forTypeProvider(new SerializableTypeWrapper.MethodInvokeTypeProvider(this.provider, method, i));
                }

                return result;
            } else {
                try {
                    return method.invoke(this.provider.getType(), args);
                } catch (InvocationTargetException var6) {
                    throw var6.getTargetException();
                }
            }
        }
    }

    interface TypeProvider extends Serializable {
        @Nullable
        Type getType();

        @Nullable
        default Object getSource() {
            return null;
        }
    }

    interface SerializableTypeProxy {
        SerializableTypeWrapper.TypeProvider getTypeProvider();
    }
}
