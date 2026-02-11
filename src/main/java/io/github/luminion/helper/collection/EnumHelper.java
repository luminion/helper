package io.github.luminion.helper.collection;

import io.github.luminion.helper.reflect.LambdaHelper;
import io.github.luminion.helper.core.SFunc;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 枚举助手，方便获取枚举中的key和value
 *
 * @author luminion
 */
public class EnumHelper<E extends Enum<E>, K> {
    private final Map<K, E> keyMap;

    private EnumHelper(Class<E> clazz, Function<E, K> getter) {
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        Map<K, E> map = new HashMap<>(enumSet.size());
        for (E e : enumSet) {
            K key = getter.apply(e);
            if (key != null) {
                if (map.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate key:[" + key + "] found in enum: " + clazz.getName());
                }
                map.put(key, e);
            }
        }
        this.keyMap = Collections.unmodifiableMap(map);
    }

    /**
     * 创建一个枚举助手实例
     * <p>
     *
     * @param getter 获取指定属性的方法引用，如 UserType::getCode
     * @param <E>    枚举类型
     * @param <K>    Key 类型
     * @return 枚举助手实例
     */
    public static <E extends Enum<E>, K> EnumHelper<E, K> of(SFunc<E, K> getter) {
        Class<E> clazz = LambdaHelper.resolveClass(getter);
        return new EnumHelper<>(clazz, getter);
    }

    /**
     * 根据属性值获取枚举实例
     *
     * @param getter 获取指定属性的方法引用，如 UserType::getCode
     * @param key    属性值
     * @param <E>    枚举类型
     * @param <K>    属性类型
     * @return 枚举实例，未找到返回 null
     */
    public static <E extends Enum<E>, K> E resolveEnum(SFunc<E, K> getter, K key) {
        Class<E> clazz = LambdaHelper.resolveClass(getter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        for (E e : enumSet) {
            K apply = getter.apply(e);
            if (apply != null && apply.equals(key)) {
                return e;
            }
        }
        return null;
    }


    /**
     * 根据key获取value
     *
     * @param keyGetter   获取指定属性的方法引用，如 UserType::getCode
     * @param valueGetter 获取指定属性的方法引用，如 UserType::getName
     * @param k           属性值
     * @param <E>         枚举类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return 值，未找到返回 null
     */
    public static <E extends Enum<E>, K, V> V resolveValue(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K k) {
        Class<E> clazz = LambdaHelper.resolveClass(keyGetter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        for (E e : enumSet) {
            K apply = keyGetter.apply(e);
            if (apply != null && apply.equals(k)) {
                return valueGetter.apply(e);
            }
        }
        return null;
    }

    /**
     * 返回键值对map
     *
     * @param keyGetter   获取指定属性的方法引用，如 UserType::getCode
     * @param valueGetter 获取指定属性的方法引用，如 UserType::getName
     * @param <E>         枚举类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return 映射的Map
     */
    public static <E extends Enum<E>, K, V> Map<K, V> resolveMap(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter) {
        Class<E> clazz = LambdaHelper.resolveClass(keyGetter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (E e : enumSet) {
            K k = keyGetter.apply(e);
            V v = valueGetter.apply(e);
            if (k != null && v != null) {
                map.put(k, valueGetter.apply(e));
            }
        }
        return map;
    }


    /**
     * 根据 key 获取枚举实例 (O(1) 复杂度)
     *
     * @param key key
     * @return 枚举实例，未找到返回 null
     */
    public E resolveEnum(K key) {
        if (key == null) {
            return null;
        }
        return keyMap.get(key);
    }

    /**
     * 快捷获取 Value
     *
     * @param key         key
     * @param valueGetter 获取值的 getter
     * @param <V>         值类型
     * @return 值，未找到返回 null
     */
    public <V> V resolveValue(K key, Function<E, V> valueGetter) {
        E e = resolveEnum(key);
        return e == null ? null : valueGetter.apply(e);
    }

    /**
     * 获取枚举键值对映射的Map
     *
     * @param valueGetter 获取值的 getter
     * @param <V>         值类型
     * @return 映射的Map
     */
    public <V> Map<K, V> resolveMap(Function<E, V> valueGetter) {
        return keyMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueGetter.apply(e.getValue())));
    }
}