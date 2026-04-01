package io.github.luminion.helper.tree;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.List;

/**
 * 树静态入口。
 * <p>
 * 只负责创建树构建器，没有独立职责。
 * 直接使用 {@link TreeBuilder#of(Function, Function, BiConsumer)} 即可。
 *
 * @author luminion
 * @deprecated 请直接使用 {@link TreeBuilder}
 */
@Deprecated
public abstract class TreeHelper {
    private TreeHelper() {}

    /**
     * 使用默认最大深度创建树构建器。
     */
    public static <T, R> TreeBuilder<T, R> of(Function<T, R> idGetter,
                                              Function<T, R> parentIdGetter,
                                              BiConsumer<T, ? super List<T>> childrenSetter) {
        return TreeBuilder.of(idGetter, parentIdGetter, childrenSetter);
    }

    /**
     * 创建带最大深度限制的树构建器。
     */
    public static <T, R> TreeBuilder<T, R> of(Function<T, R> idGetter,
                                              Function<T, R> parentIdGetter,
                                              BiConsumer<T, ? super List<T>> childrenSetter,
                                              int maxDepth) {
        return TreeBuilder.of(idGetter, parentIdGetter, childrenSetter, maxDepth);
    }
}
