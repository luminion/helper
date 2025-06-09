package io.github.bootystar.helper.base.reflect;

import com.google.common.base.Objects;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射帮手
 *
 * @author bootystar
 */
public abstract class ReflectHelper {

    private static final Map<Class<?>, Map<String, Field>> FIELD_MAP_CACHE = new ConcurrentHashMap<>();


    /**
     * 是否为java自带的核心类
     *
     * @param clazz 类
     * @return boolean
     */
    public static boolean isJavaCoreClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.getClassLoader() == null;
    }

    /**
     * 新建实例
     *
     * @param clazz 类
     * @return 类实例
     */
    @SneakyThrows
    public static <T> T newInstance(Class<T> clazz) {
        return clazz.getConstructor().newInstance();
    }

    /**
     * 指定类属性map
     *
     * @param clazz 类
     * @return 属性map
     */
    public static Map<String, Field> fieldMap(Class<?> clazz) {
        if (isJavaCoreClass(clazz)) {
            throw new IllegalArgumentException("clazz must not be java class");
        }
        Map<String, Field> stringFieldMap = FIELD_MAP_CACHE.get(clazz);
        if (stringFieldMap != null) {
            return stringFieldMap;
        }
        Map<String, Field> map = new HashMap<>();
        while (clazz != null && Object.class != clazz && !clazz.isInterface()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (isSpecialModifier(field.getModifiers())) {
                    continue;
                }
                map.putIfAbsent(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        FIELD_MAP_CACHE.put(clazz, map);
        return map;
    }

    /**
     * 是否为特殊修饰符
     *
     * @param modifiers 修饰符
     * @return boolean
     */
    public static boolean isSpecialModifier(int modifiers) {
        return Modifier.isStatic(modifiers)
                || Modifier.isFinal(modifiers)
                || Modifier.isNative(modifiers)
                || Modifier.isVolatile(modifiers)
                || Modifier.isTransient(modifiers)
                ;
    }


    /**
     * 复制属性
     *
     * @param source 来源
     * @param target 目标
     * @return 目标对象
     */
    @SneakyThrows
    public static <T> T copyFieldProperties(Object source, T target) {
        if (source == null || target == null || source.equals(target)) return target;
        Map<String, Field> sourceMap = fieldMap(source.getClass());
        Map<String, Field> targetMap = fieldMap(target.getClass());
        for (Field field : sourceMap.values()) {
            Object o = field.get(source);
            if (o == null) continue;
            Field targetFiled = targetMap.get(field.getName());
            if (targetFiled != null && targetFiled.getType().isAssignableFrom(field.getType())) {
                targetFiled.set(target, o);
            }
        }
        return target;
    }


    /**
     * 对象转map
     *
     * @param source 来源
     * @return {@link Map }
     */
    @SneakyThrows
    public static Map<?, ?> objectToMap(Object source) {
        if (source == null) return null;
        if (source instanceof Map) return (Map<?, ?>) source;
        HashMap<String, Object> map = new HashMap<>();
        Collection<Field> fields = fieldMap(source.getClass()).values();
        for (Field field : fields) {
            Object o = field.get(source);
            if (o == null) continue;
            map.put(field.getName(), o);
        }
        return map;
    }

    /**
     * 对象转对象
     *
     * @param source 来源
     * @param clazz  目标类
     */
    public static <T> T toTarget(Object source, Class<T> clazz) {
        return copyFieldProperties(source, newInstance(clazz));
    }


    /**
     * 创建两个对象的差异属性(来源对象为null的属性不会判断)
     *
     * @param source 来源
     * @param target 目标
     * @return 差异属性对象, 若相同, 返回null
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T toDifference(T source, T target) {
        if (source == null || target == null || source.equals(target)) {
            return null;
        }
        Class<T> clazz = (Class<T>) source.getClass();
        Map<String, Field> fieldMap = fieldMap(clazz);
        T instance = newInstance(clazz);
        Collection<Field> values = fieldMap.values();
        for (Field field : values) {
            Object o = field.get(source);
            Object o1 = field.get(target);
            if (Objects.equal(o,o1)) {
                continue;
            }
            field.set(instance, o);
        }
        return instance;
    }


}
