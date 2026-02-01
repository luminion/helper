package io.github.luminion.helper.reflect;

import lombok.SneakyThrows;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luminion
 * @since 1.0.0
 */
public abstract class LambdaHelper {
    private static final Map<Class<?>, SerializedLambda> LAMBDA_CACHE = new ConcurrentHashMap<>();

    /**
     * 从可序列化的方法引用中提取 SerializedLambda 信息。
     * <p>
     * 使用 lambdaClass 为 key 做一层缓存，避免每次都反射调用 writeReplace。
     *
     * @param getter 方法引用，例如 User::getName
     * @return SerializedLambda 实例
     */
    private static <T, R> SerializedLambda resolveSerializedLambda(SFunc<T, R> getter) {
        Class<?> lambdaClass = getter.getClass();
        return LAMBDA_CACHE.computeIfAbsent(lambdaClass,clazz->{
            SerializedLambda cached = LAMBDA_CACHE.get(lambdaClass);
            if (cached != null) {
                return cached;
            }
            try {
                Method writeReplaceMethod = lambdaClass.getDeclaredMethod("writeReplace");
                writeReplaceMethod.setAccessible(true);
                return (SerializedLambda) writeReplaceMethod.invoke(getter);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 从方法引用中获取其声明所在的类的 Class 对象。
     *
     * @param getter 方法引用
     * @return 声明该方法的类
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T, R> Class<T> resolveClass(SFunc<T, R> getter) {
        SerializedLambda serializedLambda = resolveSerializedLambda(getter);
        String className = serializedLambda.getImplClass().replace("/", ".");
        return (Class<T>) Class.forName(className);
    }

    /**
     * 从 getter 方法引用中获取其对应的属性名。
     *
     * @param getter 方法引用，例如 User::getName
     * @return 属性名，例如 "name"
     */
    public static <T, R> String resolvePropertyName(SFunc<T, R> getter) {
        String implMethodName = resolveSerializedLambda(getter).getImplMethodName();
        String name = implMethodName;

        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new IllegalArgumentException("Error parsing property name '" + implMethodName +
                    "'. Didn't start with 'is', 'get' or 'set'.");
        }
        return Introspector.decapitalize(name);
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        LAMBDA_CACHE.clear();
    }

    /**
     * 清除指定类的缓存
     *
     * @param clazz 类
     */
    public static void clearCache(Class<?> clazz) {
        if (clazz != null) {
            LAMBDA_CACHE.remove(clazz);
        }
    }

  
}
