package io.github.luminion.helper.reflect;

import io.github.luminion.helper.core.SFunc;
import lombok.SneakyThrows;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author luminion
 * @since 1.0.0
 */
public abstract class LambdaHelper {

    /**
     * 缓存 Lambda 相关元数据
     * 使用 ClassValue 避免 ClassLoader 内存泄漏，使用 AtomicReference 允许后置初始化（需要实例）
     */
    private static final ClassValue<AtomicReference<LambdaInfo>> CACHE = new ClassValue<AtomicReference<LambdaInfo>>() {
        @Override
        protected AtomicReference<LambdaInfo> computeValue(Class<?> type) {
            return new AtomicReference<>();
        }
    };

    /**
     * Lambda 元数据内部类
     */
    private static class LambdaInfo {
        private final SerializedLambda serializedLambda;
        private final Class<?> implClass;
        private final String propertyName;

        @SneakyThrows
        public LambdaInfo(Object getter) {
            // 1. 提取 SerializedLambda
            Method writeReplaceMethod = getter.getClass().getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            this.serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(getter);

            // 2. 解析 ImplClass
            String className = serializedLambda.getImplClass().replace("/", ".");
            this.implClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

            // 3. 解析 PropertyName
            String implMethodName = serializedLambda.getImplMethodName();
            String name = implMethodName;
            if (name.startsWith("is")) {
                name = name.substring(2);
            } else if (name.startsWith("get") || name.startsWith("set")) {
                name = name.substring(3);
            } else {
                throw new IllegalArgumentException("Error parsing property name '" + implMethodName +
                        "'. Didn't start with 'is', 'get' or 'set'.");
            }
            this.propertyName = Introspector.decapitalize(name);
        }
    }

    /**
     * 获取 LambdaInfo (带缓存)
     */
    private static <T, R> LambdaInfo resolveLambdaInfo(SFunc<T, R> getter) {
        Class<?> lambdaClass = getter.getClass();
        AtomicReference<LambdaInfo> ref = CACHE.get(lambdaClass);
        LambdaInfo info = ref.get();
        if (info == null) {
            info = new LambdaInfo(getter);
            if (!ref.compareAndSet(null, info)) {
                info = ref.get();
            }
        }
        return info;
    }

    /**
     * 从方法引用中获取其声明所在的类的 Class 对象。
     *
     * @param getter 方法引用
     * @return 声明该方法的类
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Class<T> resolveClass(SFunc<T, R> getter) {
        return (Class<T>) resolveLambdaInfo(getter).implClass;
    }

    /**
     * 从 getter 方法引用中获取其对应的属性名。
     *
     * @param getter 方法引用，例如 User::getName
     * @return 属性名，例如 "name"
     */
    public static <T, R> String resolvePropertyName(SFunc<T, R> getter) {
        return resolveLambdaInfo(getter).propertyName;
    }
}
