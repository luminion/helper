package io.github.luminion.helper.reflect;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 反射帮手
 *
 * @author luminion
 */
@Slf4j
public abstract class BeanHelper {

    /**
     * 使用 ClassValue 缓存 Bean 的属性描述符。
     * ClassValue 是 JDK 提供的用于关联 Class 数据的标准方式，能自动处理弱引用和 GC。
     */
    private static final ClassValue<Map<String, PropertyDescriptor>> PD_CACHE = new ClassValue<Map<String, PropertyDescriptor>>() {
        @Override
        @SneakyThrows
        protected Map<String, PropertyDescriptor> computeValue(Class<?> clazz) {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            Map<String, PropertyDescriptor> map = new LinkedHashMap<>(pds.length);
            for (PropertyDescriptor pd : pds) {
                if ("class".equals(pd.getName())) {
                    continue;
                }
                map.put(pd.getName(), pd);
            }
            return Collections.unmodifiableMap(map);
        }
    };

    /**
     * 新建实例
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 类实例
     */
    @SneakyThrows
    public static <T> T newInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        return clazz.getConstructor().newInstance();
    }

    /**
     * 指定类属性map
     *
     * @param clazz 类
     * @return 属性map（不可修改）
     */
    public static Map<String, PropertyDescriptor> propertyMap(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        return PD_CACHE.get(clazz);
    }

    /**
     * 复制属性
     *
     * @param source 来源
     * @param target 目标
     * @param <T>    目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, T target) {
        return copyProperties(source, target, true);
    }

    /**
     * 复制属性（包含null值）
     *
     * @param source 来源
     * @param target 目标
     * @param <T>    目标类型
     * @return 目标对象
     */
    public static <T> T copyPropertiesWithNull(Object source, T target) {
        return copyProperties(source, target, false);
    }

    /**
     * 复制属性（核心实现）
     *
     * @param source     来源
     * @param target     目标
     * @param ignoreNull 是否忽略null值
     */
    @SneakyThrows
    private static <T> T copyProperties(Object source, T target, boolean ignoreNull) {
        if (source == null || target == null || source == target) {
            return target;
        }
        Map<String, PropertyDescriptor> sourceMap = propertyMap(source.getClass());
        Map<String, PropertyDescriptor> targetMap = propertyMap(target.getClass());

        for (Map.Entry<String, PropertyDescriptor> entry : sourceMap.entrySet()) {
            PropertyDescriptor sourcePd = entry.getValue();
            PropertyDescriptor targetPd = targetMap.get(entry.getKey());

            if (targetPd == null) {
                continue;
            }

            Method readMethod = sourcePd.getReadMethod();
            Method writeMethod = targetPd.getWriteMethod();

            if (readMethod != null && writeMethod != null) {
                // 读取源值
                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                    readMethod.setAccessible(true);
                }
                // check if method is accessible logic is handled by setAccessible if needed

                Object value = readMethod.invoke(source);

                if (ignoreNull && value == null) {
                    continue;
                }

                // 写入目标值
                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                    writeMethod.setAccessible(true);
                }
                writeMethod.invoke(target, value);
            }
        }
        return target;
    }

    /**
     * 对象转map
     * 只保留既有getter也有setter的方法
     *
     * @param source 来源
     * @return Map
     */
    public static Map<String, Object> objectToMap(Object source) {
        return objectToMap(source, true);
    }

    /**
     * 对象转map（包含null值）
     * 只保留既有getter也有setter的方法
     *
     * @param source 来源
     * @return Map
     */
    public static Map<String, Object> objectToMapWithNull(Object source) {
        return objectToMap(source, false);
    }

    /**
     * 对象转Map核心实现
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectToMap(Object source, boolean ignoreNull) {
        if (source == null) {
            return null;
        }
        if (source instanceof Map) {
            return (Map<String, Object>) source;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, PropertyDescriptor> propertyMap = propertyMap(source.getClass());

        for (Map.Entry<String, PropertyDescriptor> entry : propertyMap.entrySet()) {
            PropertyDescriptor pd = entry.getValue();
            Method readMethod = pd.getReadMethod();
            Method writeMethod = pd.getWriteMethod();

            // 针对map, 只保留既有getter也有setter的方法
            if (readMethod != null && writeMethod != null) {
                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                    readMethod.setAccessible(true);
                }
                Object value = readMethod.invoke(source);

                if (ignoreNull && value == null) {
                    continue;
                }
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    /**
     * 对象转对象
     *
     * @param source 来源
     * @param clazz  目标类
     * @param <T>    目标类型
     * @return 目标对象
     */
    public static <T> T toTarget(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        return copyProperties(source, newInstance(clazz));
    }

    /**
     * 创建两个对象的差异属性(来源对象为null的属性不会判断)
     *
     * @param source 来源
     * @param target 目标
     * @param <T>    类型
     * @return 差异属性对象, 若无差异返回null
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T toDifference(T source, T target) {
        if (source == null || target == null) {
            return null;
        }
        if (source == target || source.equals(target)) {
            return null;
        }

        Class<T> clazz = (Class<T>) source.getClass();
        Map<String, PropertyDescriptor> propertyMap = propertyMap(clazz);
        T instance = null;
        boolean hasDifference = false;

        for (PropertyDescriptor pd : propertyMap.values()) {
            Method readMethod = pd.getReadMethod();
            if (readMethod == null) {
                continue;
            }

            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                readMethod.setAccessible(true);
            }
            Object sourceValue = readMethod.invoke(source);

            // 来源为null不判断
            if (sourceValue == null) {
                continue;
            }

            Object targetValue = readMethod.invoke(target);
            if (!Objects.equals(sourceValue, targetValue)) {
                if (!hasDifference) {
                    instance = newInstance(clazz);
                    hasDifference = true;
                }
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null) {
                    if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                        writeMethod.setAccessible(true);
                    }
                    writeMethod.invoke(instance, sourceValue);
                }
            }
        }
        return instance;
    }

    /**
     * 获取属性值
     *
     * @param target       目标对象
     * @param propertyName 属性名
     * @return 属性值
     */
    @SneakyThrows
    public static Object getPropertyValue(Object target, String propertyName) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(propertyName, "propertyName must not be null");

        PropertyDescriptor pd = propertyMap(target.getClass()).get(propertyName);
        if (pd == null || pd.getReadMethod() == null) {
            throw new NoSuchMethodException(
                    "Property '" + propertyName + "' or its getter not found in " + target.getClass().getName());
        }
        Method readMethod = pd.getReadMethod();
        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
            readMethod.setAccessible(true);
        }
        return readMethod.invoke(target);
    }

    /**
     * 设置属性值
     *
     * @param target       目标对象
     * @param propertyName 属性名
     * @param value        值
     */
    @SneakyThrows
    public static void setPropertyValue(Object target, String propertyName, Object value) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(propertyName, "propertyName must not be null");

        PropertyDescriptor pd = propertyMap(target.getClass()).get(propertyName);
        if (pd == null || pd.getWriteMethod() == null) {
            throw new NoSuchMethodException(
                    "Property '" + propertyName + "' or its setter not found in " + target.getClass().getName());
        }
        Method writeMethod = pd.getWriteMethod();
        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
            writeMethod.setAccessible(true);
        }
        writeMethod.invoke(target, value);
    }
}