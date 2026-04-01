package io.github.luminion.helper.tree;

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
 * 树构建器。
 *
 * @author luminion
 */
public class TreeBuilder<T, R> {
    private final int maxDepth;
    private final Function<T, R> idGetter;
    private final Function<T, R> parentIdGetter;
    private final BiConsumer<T, ? super List<T>> childrenSetter;

    public TreeBuilder(int maxDepth, Function<T, R> idGetter, Function<T, R> parentIdGetter,
                       BiConsumer<T, ? super List<T>> childrenSetter) {
        this.maxDepth = maxDepth;
        this.idGetter = idGetter;
        this.parentIdGetter = parentIdGetter;
        this.childrenSetter = childrenSetter;
    }

    public static <T, R> TreeBuilder<T, R> of(Function<T, R> idGetter,
                                              Function<T, R> parentIdGetter,
                                              BiConsumer<T, ? super List<T>> childrenSetter) {
        return new TreeBuilder<T, R>(100, idGetter, parentIdGetter, childrenSetter);
    }

    public static <T, R> TreeBuilder<T, R> of(Function<T, R> idGetter,
                                              Function<T, R> parentIdGetter,
                                              BiConsumer<T, ? super List<T>> childrenSetter,
                                              int maxDepth) {
        return new TreeBuilder<T, R>(maxDepth, idGetter, parentIdGetter, childrenSetter);
    }

    public List<T> buildRelation(Collection<? extends T> elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        // 先按 parentId 建索引，后续每个节点都能快速拿到直接子节点列表。
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
        return buildRelation(elements).stream().filter(actualFilter).collect(Collectors.toList());
    }

    public List<T> treeRootByParentId(Collection<? extends T> elements, final R parentId) {
        return treeRoot(elements, new Predicate<T>() {
            @Override
            public boolean test(T element) {
                return Objects.equals(parentIdGetter.apply(element), parentId);
            }
        });
    }

    public List<T> treeRootAuto(Collection<? extends T> elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<R> idSet = elements.stream().map(idGetter).collect(Collectors.toSet());
        return treeRoot(elements, new Predicate<T>() {
            @Override
            public boolean test(T element) {
                R parentId = parentIdGetter.apply(element);
                return parentId == null || !idSet.contains(parentId);
            }
        });
    }

    public T treeById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        buildRelation(elements);
        return elements.stream().filter(e -> Objects.equals(idGetter.apply(e), id)).findFirst().orElse(null);
    }

    public T treeByNode(Collection<? extends T> elements, T node) {
        return node == null ? null : treeById(elements, idGetter.apply(node));
    }

    public List<T> findDirectChildrenById(Collection<? extends T> elements, R id) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        return elements.stream().filter(c -> Objects.equals(id, parentIdGetter.apply(c))).collect(Collectors.toList());
    }

    public List<T> findDirectChildrenByNode(Collection<? extends T> elements, T node) {
        return node == null ? Collections.<T>emptyList() : findDirectChildrenById(elements, idGetter.apply(node));
    }

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

    public List<T> findAllChildrenByNode(Collection<? extends T> elements, T node) {
        return node == null ? Collections.<T>emptyList() : findAllChildrenById(elements, idGetter.apply(node));
    }

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

    public List<T> findAllParentByNode(Collection<? extends T> elements, T node) {
        return node == null ? Collections.<T>emptyList() : findAllParentById(elements, idGetter.apply(node));
    }

    private void collectChildrenRecursively(Map<R, List<T>> parentMap, R currentId, List<T> result, Set<R> visited,
                                            int depth) {
        if (depth >= maxDepth) {
            return;
        }
        List<T> children = parentMap.get(currentId);
        if (children == null || children.isEmpty()) {
            return;
        }

        for (T child : children) {
            R childId = idGetter.apply(child);
            // 用 visited 兜底，避免脏数据形成环时递归跑死。
            if (childId != null && !visited.add(childId)) {
                continue;
            }
            result.add(child);
            collectChildrenRecursively(parentMap, childId, result, visited, depth + 1);
        }
    }

    private Map<R, List<T>> buildParentMap(Collection<? extends T> elements) {
        // 父节点索引是所有树操作的基础，统一在这里生成，避免多个方法重复遍历。
        return elements.stream()
                .filter(e -> parentIdGetter.apply(e) != null)
                .collect(Collectors.groupingBy(parentIdGetter, LinkedHashMap::new,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
