package io.github.luminion.helper.collection;

import io.github.luminion.helper.core.SFunc;
import io.github.luminion.helper.reflect.LambdaHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.enums.EnumHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.enums.EnumHelper}
 */
@Deprecated
public class EnumHelper<E extends Enum<E>, K> extends io.github.luminion.helper.enums.EnumResolver<E, K> {

    private EnumHelper(Class<E> clazz, Function<E, K> getter) {
        super(clazz, getter);
    }

    public static <E extends Enum<E>, K> EnumHelper<E, K> of(SFunc<E, K> getter) {
        Class<E> clazz = LambdaHelper.resolveClass(getter);
        return new EnumHelper<E, K>(clazz, getter);
    }

    public static <E extends Enum<E>, K> EnumHelper<E, K> of(Class<E> clazz, Function<E, K> getter) {
        return new EnumHelper<E, K>(clazz, getter);
    }

    public static <E extends Enum<E>, K> E resolveEnum(SFunc<E, K> getter, K key) {
        return io.github.luminion.helper.enums.EnumHelper.resolveEnum(getter, key);
    }

    public static <E extends Enum<E>, K, V> V resolveValue(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K key) {
        return io.github.luminion.helper.enums.EnumHelper.resolveValue(keyGetter, valueGetter, key);
    }

    public static <E extends Enum<E>, K, V> List<V> resolveValues(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K... keys) {
        return io.github.luminion.helper.enums.EnumHelper.resolveValues(keyGetter, valueGetter, keys);
    }

    public static <E extends Enum<E>, K, V> List<K> resolveKeys(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, V... values) {
        return io.github.luminion.helper.enums.EnumHelper.resolveKeys(keyGetter, valueGetter, values);
    }

    public static <E extends Enum<E>, K, V> Map<K, V> resolveMap(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter) {
        return io.github.luminion.helper.enums.EnumHelper.resolveMap(keyGetter, valueGetter);
    }
}
