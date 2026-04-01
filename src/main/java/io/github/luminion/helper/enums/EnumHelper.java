package io.github.luminion.helper.enums;

import io.github.luminion.helper.core.SFunc;
import io.github.luminion.helper.reflect.LambdaHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 枚举静态入口。
 * <p>
 * 适合临时查询；如果同一枚举会被反复按 key 查找，建议先创建 {@link EnumResolver} 复用索引。
 *
 * @author luminion
 */
public abstract class EnumHelper {
    private EnumHelper() {}

    /**
     * 通过方法引用推导枚举类型并创建解析器。
     */
    public static <E extends Enum<E>, K> EnumResolver<E, K> of(SFunc<E, K> getter) {
        Class<E> clazz = LambdaHelper.resolveClass(getter);
        return EnumResolver.of(clazz, getter);
    }

    /**
     * 显式指定枚举类型并创建解析器。
     */
    public static <E extends Enum<E>, K> EnumResolver<E, K> of(Class<E> clazz, Function<E, K> getter) {
        return EnumResolver.of(clazz, getter);
    }

    /**
     * 根据 key 解析枚举值。
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

    public static <E extends Enum<E>, K, V> V resolveValue(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K key) {
        E resolved = resolveEnum(keyGetter, key);
        return resolved == null ? null : valueGetter.apply(resolved);
    }

    public static <E extends Enum<E>, K, V> List<V> resolveValues(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K... keys) {
        if (keys == null || keys.length == 0) {
            return Collections.emptyList();
        }
        Class<E> clazz = LambdaHelper.resolveClass(keyGetter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        Set<K> keySet = new HashSet<K>(Arrays.asList(keys));
        List<V> values = new ArrayList<V>();
        for (E e : enumSet) {
            K key = keyGetter.apply(e);
            if (key != null && keySet.contains(key)) {
                V value = valueGetter.apply(e);
                if (value != null) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    public static <E extends Enum<E>, K, V> List<K> resolveKeys(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, V... values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }
        Class<E> clazz = LambdaHelper.resolveClass(keyGetter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        Set<V> valueSet = new HashSet<V>(Arrays.asList(values));
        List<K> keys = new ArrayList<K>();
        for (E e : enumSet) {
            V value = valueGetter.apply(e);
            if (value != null && valueSet.contains(value)) {
                K key = keyGetter.apply(e);
                if (key != null) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    public static <E extends Enum<E>, K, V> Map<K, V> resolveMap(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter) {
        Class<E> clazz = LambdaHelper.resolveClass(keyGetter);
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        Map<K, V> map = new LinkedHashMap<K, V>();
        for (E e : enumSet) {
            K key = keyGetter.apply(e);
            V value = valueGetter.apply(e);
            if (key != null && value != null) {
                map.put(key, value);
            }
        }
        return map;
    }
}
