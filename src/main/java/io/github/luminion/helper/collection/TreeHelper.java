package io.github.luminion.helper.collection;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.tree.TreeHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.tree.TreeHelper}
 */
@Deprecated
public class TreeHelper<T, R> extends io.github.luminion.helper.tree.TreeBuilder<T, R> {

    private TreeHelper(int maxDepth, Function<T, R> idGetter, Function<T, R> parentIdGetter,
                       BiConsumer<T, ? super List<T>> childrenSetter) {
        super(maxDepth, idGetter, parentIdGetter, childrenSetter);
    }

    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter,
                                             Function<T, R> parentIdGetter,
                                             BiConsumer<T, ? super List<T>> childrenSetter) {
        return new TreeHelper<T, R>(100, idGetter, parentIdGetter, childrenSetter);
    }

    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter,
                                             Function<T, R> parentIdGetter,
                                             BiConsumer<T, ? super List<T>> childrenSetter,
                                             int maxDepth) {
        return new TreeHelper<T, R>(maxDepth, idGetter, parentIdGetter, childrenSetter);
    }
}
