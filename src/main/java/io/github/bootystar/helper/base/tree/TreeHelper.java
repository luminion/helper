package io.github.bootystar.helper.base.tree;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树状工具助手
 *
 * @author bootystar
 */
@RequiredArgsConstructor
public class TreeHelper<T, R> {
    private final Function<T, R> idGetter;
    private final Function<T, R> parentIdGetter;
    private final BiConsumer<T, ? super List<T>> childrenSetter;

    /**
     * 创建树助手
     *
     * @param idGetter           id getter
     * @param parentIdGetter     父id getter
     * @param childrenSetter     子元素setter
     * @return {@link TreeHelper } 对应的树助手
     */
    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter, 
                                             Function<T, R> parentIdGetter, 
                                             BiConsumer<T, ? super List<T>> childrenSetter) {
        return new TreeHelper<>(idGetter, parentIdGetter, childrenSetter);
    }

    /**
     * 建立关系
     *
     * @param elements 源列表
     * @return {@link List } 源列表
     */
    public List<T> buildRelation(Collection<? extends T> elements) {
        ArrayList<T> ts = new ArrayList<>(elements);
        ts.forEach(e -> childrenSetter.accept(e, findDirectChildrenByNode(ts, e)));
        return ts;
    }

    /**
     * 构建根目录树
     *
     * @param elements  元素
     * @param filter 根目录元素过滤规则
     * @return {@link List } 根目录元素
     */
    public List<T> treeRoot(Collection<? extends T> elements, Predicate<? super T> filter) {
        return buildRelation(elements).stream().filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * 构建以指定id节点作为根节点的树
     *
     * @param elements 元素
     * @param id       id
     * @return 指定id对应节点
     */
    public T treeById(Collection<? extends T> elements, R id) {
        return buildRelation(elements).stream()
                .filter(e -> Objects.equals(idGetter.apply(e), id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 构建以当前节点作为根节点的树
     *
     * @param elements 元素
     * @param node     节点
     * @return 节点
     */
    public T treeByNode(Collection<? extends T> elements, T node) {
        return treeById(elements, idGetter.apply(node));
    }

    /**
     * 获取指定id节点对应的子集
     *
     * @param elements 元素
     * @param id       id
     * @return 直接子元素列表
     */
    public List<T> findDirectChildrenById(Collection<? extends T> elements, R id) {
        return elements.stream()
                .filter(c -> Objects.equals(id, parentIdGetter.apply(c)))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前节点对应的子集
     *
     * @param elements 元素
     * @param node     节点
     * @return 直接子元素列表
     */
    public List<T> findDirectChildrenByNode(Collection<? extends T> elements, T node) {
        return elements.stream()
                .filter(c -> Objects.equals(idGetter.apply(node), parentIdGetter.apply(c)))
                .collect(Collectors.toList());
    }

    /**
     * 检索指定id对应的所有子节点
     *
     * @param elements 元素
     * @param id       id
     * @return 子元素列表
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
     * @return 子元素列表
     */
    public List<T> findAllChildrenByNode(Collection<? extends T> elements, T node) {
        return findAllChildrenById(elements, idGetter.apply(node));
    }

    /**
     * 查找指定id节点对应的所有父节点
     *
     * @param elements 元素
     * @param id       id
     * @return 父元素列表
     */
    public List<T> findAllParentById(Collection<? extends T> elements, R id) {
        T currentNode = elements.stream()
                .filter(c -> Objects.equals(id, idGetter.apply(c)))
                .findFirst()
                .orElse(null);
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
     * @return 父元素列表
     */
    public List<T> findAllParentByNode(Collection<? extends T> elements, T node) {
        return findAllParentById(elements, idGetter.apply(node));
    }

}
