package io.github.luminion.helper.collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树状工具助手
 *
 * @author luminion
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeHelper<T, R> {
    /**
     * 最大深度
     */
    private final int maxDepth;
    private final Function<T, R> idGetter;
    private final Function<T, R> parentIdGetter;
    private final BiConsumer<T, ? super List<T>> childrenSetter;

    /**
     * 创建树助手
     *
     * @param idGetter       id getter
     * @param parentIdGetter 父id getter
     * @param childrenSetter 子元素setter
     * @return {@link TreeHelper } 对应的树助手
     */
    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter,
            Function<T, R> parentIdGetter,
            BiConsumer<T, ? super List<T>> childrenSetter) {
        return new TreeHelper<>(100, idGetter, parentIdGetter, childrenSetter);
    }

    /**
     * 创建树助手（指定最大深度）
     */
    public static <T, R> TreeHelper<T, R> of(Function<T, R> idGetter,
            Function<T, R> parentIdGetter,
            BiConsumer<T, ? super List<T>> childrenSetter,
            int maxDepth) {
        return new TreeHelper<>(maxDepth, idGetter, parentIdGetter, childrenSetter);
    }

    /**
     * 建立关系
     * <p>
     * 注意：此方法会修改传入对象的 children 属性。
     *
     * @param elements 源列表
     * @return {@link List } 源列表
     */
    public List<T> buildRelation(Collection<? extends T> elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        Map<R, List<T>> parentMap = buildParentMap(elements);

        for (T element : elements) {
            R myId = idGetter.apply(element);
            List<T> children = parentMap.get(myId);
            if (children == null) {
                children = Collections.emptyList();
            } else {
                children = new ArrayList<T>(children);
            }
            childrenSetter.accept(element, children);
        }
        return new ArrayList<T>(elements);
    }

    /**
     * 构建根目录树
     *
     * @param elements 元素
     * @param filter   根目录元素过滤规则
     * @return {@link List } 根目录元素
     */
    public List<T> treeRoot(Collection<? extends T> elements, Predicate<? super T> filter) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        Predicate<? super T> actualFilter = filter == null ? new Predicate<T>() {
            @Override
            public boolean test(T t) {
                return true;
            }
        } : filter;
        return buildRelation(elements).stream().filter(actualFilter)
                .collect(Collectors.toList());
    }

    /**
     * 根据指定父 id 构建根节点列表。
     */
    public List<T> treeRootByParentId(Collection<? extends T> elements, final R parentId) {
        return treeRoot(elements, new Predicate<T>() {
            @Override
            public boolean test(T element) {
                return Objects.equals(parentIdGetter.apply(element), parentId);
            }
        });
    }

    /**
     * 自动识别根节点：parentId 为 null 或父节点不存在时视为根节点。
     */
    public List<T> treeRootAuto(Collection<? extends T> elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        Set<R> idSet = elements.stream().map(idGetter).collect(Collectors.toSet());
        return treeRoot(elements, new Predicate<T>() {
            @Override
            public boolean test(T element) {
                R parentId = parentIdGetter.apply(element);
                return parentId == null || !idSet.contains(parentId);
            }
        });
    }

    /**
     * 构建以指定id节点作为根节点的树
     *
     * @param elements 元素
     * @param id       id
     * @return 指定id对应节点
     */
    public T treeById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        buildRelation(elements);
        return elements.stream()
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
        return node == null ? null : treeById(elements, idGetter.apply(node));
    }

    /**
     * 获取指定id节点对应的直属子集
     *
     * @param elements 元素
     * @param id       id
     * @return 直接子元素列表
     */
    public List<T> findDirectChildrenById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        return elements.stream()
                .filter(c -> Objects.equals(id, parentIdGetter.apply(c)))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前节点对应的直属子集
     *
     * @param elements 元素
     * @param node     节点
     * @return 直接子元素列表
     */
    public List<T> findDirectChildrenByNode(Collection<? extends T> elements, T node) {
        return node == null ? Collections.<T>emptyList() : findDirectChildrenById(elements, idGetter.apply(node));
    }

    /**
     * 检索指定id对应的所有子节点（递归）
     *
     * @param elements 元素
     * @param id       id
     * @return 子元素列表
     */
    public List<T> findAllChildrenById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        Map<R, List<T>> parentMap = buildParentMap(elements);
        List<T> result = new ArrayList<T>();
        Set<R> visited = new HashSet<R>();
        if (id != null) {
            visited.add(id);
        }
        collectChildrenRecursively(parentMap, id, result, visited, 0);
        return result;
    }

    /**
     * 沿首个子分支递归收集子节点，并在脏数据成环时停止。
     */
    private void collectChildrenRecursively(Map<R, List<T>> parentMap, R currentId, List<T> result, Set<R> visited, int depth) {
        if (depth >= maxDepth) {
            return;
        }
        List<T> children = parentMap.get(currentId);
        if (children == null || children.isEmpty()) {
            return;
        }

        for (T child : children) {
            R childId = idGetter.apply(child);
            if (childId != null && !visited.add(childId)) {
                continue;
            }
            result.add(child);
            collectChildrenRecursively(parentMap, childId, result, visited, depth + 1);
            return;
        }
    }

    /**
     * 检索指定节点对应的所有子节点
     *
     * @param elements 元素
     * @param node     节点
     * @return 子元素列表
     */
    public List<T> findAllChildrenByNode(Collection<? extends T> elements, T node) {
        return node == null ? Collections.emptyList() : findAllChildrenById(elements, idGetter.apply(node));
    }

    /**
     * 查找指定id节点对应的所有父节点
     *
     * @param elements 元素
     * @param id       id
     * @return 父元素列表
     */
    public List<T> findAllParentById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        Map<R, T> idMap = elements.stream()
                .collect(Collectors.toMap(idGetter, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));

        T currentNode = idMap.get(id);
        List<T> parents = new ArrayList<T>();
        if (currentNode == null) {
            return parents;
        }

        Set<R> visited = new HashSet<R>();
        visited.add(id);

        R parentId = parentIdGetter.apply(currentNode);
        int depth = 0;

        while (parentId != null && depth < maxDepth) {
            if (visited.contains(parentId)) {
                break;
            }
            visited.add(parentId);

            T parent = idMap.get(parentId);
            if (parent == null) {
                break;
            }
            parents.add(parent);
            parentId = parentIdGetter.apply(parent);
            depth++;
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
        return node == null ? Collections.<T>emptyList() : findAllParentById(elements, idGetter.apply(node));
    }

    private Map<R, List<T>> buildParentMap(Collection<? extends T> elements) {
        return elements.stream()
                .filter(e -> parentIdGetter.apply(e) != null)
                .collect(Collectors.groupingBy(parentIdGetter, LinkedHashMap::new, Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
