package io.github.bootystar.helper.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树状工具助手
 *
 * @author bootystar
 */
public class TreeHelper<T, R> {
    private final Function<T, R> idGetter;
    private final Function<T, R> parentIdGetter;
    private final BiConsumer<T, ? super List<T>> childElementSetter;

    /**
     * 创建树助手
     *
     * @param idGetter           id getter
     * @param parentIdGetter     父id getter
     * @param childElementSetter 子元素setter
     * @return {@link TreeHelper }<{@link T }, {@link R }>
     * @author bootystar
     */
    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter, Function<T, R> parentIdGetter, BiConsumer<T, ? super List<T>> childElementSetter) {
        return new TreeHelper<>(idGetter, parentIdGetter, childElementSetter);
    }

    /**
     * 树助手
     *
     * @param idGetter           id getter
     * @param parentIdGetter     父id getter
     * @param childElementSetter 子元素setter
     * @author bootystar
     */
    private TreeHelper(Function<T, R> idGetter, Function<T, R> parentIdGetter, BiConsumer<T, ? super List<T>> childElementSetter) {
        if (idGetter == null || parentIdGetter == null || childElementSetter == null) {
            throw new IllegalArgumentException("arguments can not be null" );
        }
        this.idGetter = idGetter;
        this.parentIdGetter = parentIdGetter;
        this.childElementSetter = childElementSetter;
    }

    /**
     * 建立关系
     *
     * @param elements 源列表
     * @return {@link List }<{@link ? } {@link extends } {@link T }>
     * @author bootystar
     */
    public List<? extends T> buildRelation(Collection<? extends T> elements) {
        ArrayList<T> ts = new ArrayList<>(elements);
        ts.forEach(e -> childElementSetter.accept(e, findDirectChildrenByNode(ts, e)));
        return ts;
    }

    /**
     * 构建根目录树
     *
     * @param elements 元素
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<? extends T> treeRoot(Collection<? extends T> elements) {
        return buildRelation(elements).stream().filter(e -> parentIdGetter.apply(e) == null).collect(Collectors.toList());
    }

    /**
     * 构建以指定id节点作为根节点的树
     *
     * @param elements 元素
     * @param id       id
     * @return {@link T }
     * @author bootystar
     */
    public T treeById(Collection<? extends T> elements, R id) {
        return buildRelation(elements).stream().filter(e -> Objects.equals(idGetter.apply(e), id)).findFirst().orElse(null);
    }

    /**
     * 构建以当前节点作为根节点的树
     *
     * @param elements 元素
     * @param node     节点
     * @return {@link T }
     * @author bootystar
     */
    public T treeByNode(Collection<? extends T> elements, T node) {
        return treeById(elements, idGetter.apply(node));
    }

    /**
     * 获取指定id节点对应的子集
     *
     * @param elements 元素
     * @param id       id
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findDirectChildrenById(Collection<? extends T> elements, R id) {
        return elements.stream().filter(c -> Objects.equals(id, parentIdGetter.apply(c))).collect(Collectors.toList());
    }

    /**
     * 获取当前节点对应的子集
     *
     * @param elements 元素
     * @param node     节点
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findDirectChildrenByNode(Collection<? extends T> elements, T node) {
        return elements.stream().filter(c -> Objects.equals(idGetter.apply(node), parentIdGetter.apply(c))).collect(Collectors.toList());
    }

    /**
     * 检索指定id对应的所有子节点
     *
     * @param elements 元素
     * @param id       id
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findAllChildrenById(Collection<? extends T> elements, R id) {
        ArrayList<T> all = new ArrayList<>(elements);
        List<T> children = findDirectChildrenById(all, id);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<T> result = new ArrayList<>(children);
        for (T child : children) {
            List<T> allChildren = this.findAllChildrenById(all, idGetter.apply(child));
            if (allChildren == null || allChildren.isEmpty()) {
                continue;
            }
            result.addAll(allChildren);
        }
        return result;
    }

    /**
     * 检索指定节点对应的所有子节点
     *
     * @param elements 元素
     * @param node     节点
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findAllChildrenByNode(Collection<? extends T> elements, T node) {
        return findAllChildrenById(elements, idGetter.apply(node));
    }

    /**
     * 查找指定id节点对应的所有父节点
     *
     * @param elements 元素
     * @param id       id
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findAllParentById(Collection<? extends T> elements, R id) {
        T currentNode = elements.stream().filter(c -> Objects.equals(id, idGetter.apply(c))).findFirst().orElse(null);
        ArrayList<T> parents = new ArrayList<>();
        if (currentNode == null) {
            return parents;
        }
        R parentId = parentIdGetter.apply(currentNode);
        while (parentId != null) {
            for (T element : elements) {
                if (Objects.equals(parentId, idGetter.apply(element))) {
                    parents.add(element);
                    parentId = parentIdGetter.apply(element);
                    break;
                }
            }
        }
        return parents;
    }

    /**
     * 查找指定节点对应的所有父节点
     *
     * @param elements 元素
     * @param node     节点
     * @return {@link List }<{@link T }>
     * @author bootystar
     */
    public List<T> findAllParentByNode(Collection<? extends T> elements, T node) {
        return findAllParentById(elements, idGetter.apply(node));
    }

}
