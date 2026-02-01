package io.github.luminion.helper.collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.*;
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
        Map<R, List<T>> parentMap = elements.stream()
                .filter(e -> parentIdGetter.apply(e) != null)
                .collect(Collectors.groupingBy(parentIdGetter));

        elements.forEach(e -> {
            R myId = idGetter.apply(e);
            List<T> children = parentMap.get(myId);
            if (children != null) {
                childrenSetter.accept(e, children);
            }
        });
        return new ArrayList<>(elements);
    }

    /**
     * 构建根目录树
     *
     * @param elements 元素
     * @param filter   根目录元素过滤规则
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
        // 先构建全量关系，再提取
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
        return treeById(elements, idGetter.apply(node));
    }

    /**
     * 获取指定id节点对应的直属子集
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
     * 获取当前节点对应的直属子集
     *
     * @param elements 元素
     * @param node     节点
     * @return 直接子元素列表
     */
    public List<T> findDirectChildrenByNode(Collection<? extends T> elements, T node) {
        return findDirectChildrenById(elements, idGetter.apply(node));
    }

    /**
     * 检索指定id对应的所有子节点（递归）
     *
     * @param elements 元素
     * @param id       id
     * @return 子元素列表
     */
    public List<T> findAllChildrenById(Collection<? extends T> elements, R id) {
        // 预处理：构建 ParentID -> Children 索引，复杂度 O(N)
        Map<R, List<T>> parentMap = elements.stream()
                .filter(e -> parentIdGetter.apply(e) != null)
                .collect(Collectors.groupingBy(parentIdGetter));

        List<T> result = new ArrayList<>();
        Set<R> visited = new HashSet<>();
        // 初始节点不加入visited，因为它不在结果集中，但如果作为子节点再次出现则需要校验
        // 不过通常我们关注的是遍历过程中是否遇到已处理过的子节点

        collectChildrenRecursively(parentMap, id, result, visited);
        return result;
    }

    /**
     * 递归收集子节点（带循环检测）
     */
    private void collectChildrenRecursively(Map<R, List<T>> parentMap, R currentId, List<T> result, Set<R> visited) {
        List<T> children = parentMap.get(currentId);
        if (children == null || children.isEmpty()) {
            return;
        }

        for (T child : children) {
            R childId = idGetter.apply(child);
            // 如果已经访问过该子节点ID，说明存在循环引用，跳过
            if (!visited.add(childId)) {
                continue;
            }
            result.add(child);
            if (visited.size() > maxDepth) {
                continue;
            }
            collectChildrenRecursively(parentMap, childId, result, visited);
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
        // 预处理：构建 ID -> Node 索引，复杂度 O(N)
        Map<R, T> idMap = elements.stream()
                .collect(Collectors.toMap(idGetter, Function.identity(), (v1, v2) -> v1));

        T currentNode = idMap.get(id);
        List<T> parents = new ArrayList<>();
        if (currentNode == null) {
            return parents;
        }

        // 使用Set记录已访问的节点ID，防止循环引用
        Set<R> visited = new HashSet<>();
        visited.add(id);

        R parentId = parentIdGetter.apply(currentNode);
        int depth = 0;

        while (parentId != null && depth < maxDepth) {
            // 检测循环引用
            if (visited.contains(parentId)) {
                break;
            }
            visited.add(parentId);

            T parent = idMap.get(parentId);
            if (parent != null) {
                parents.add(parent);
                parentId = parentIdGetter.apply(parent);
                depth++;
            } else {
                break; // 找不到父节点，链条中断
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
