package io.github.luminion.helper.collection;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TreeHelperTest {

    @Test
    void shouldBuildTreeAndFindParentsAndChildren() {
        List<Node> nodes = Arrays.asList(
                new Node(1, null),
                new Node(2, 1),
                new Node(3, 1),
                new Node(4, 2)
        );
        TreeHelper<Node, Integer> helper = TreeHelper.of(Node::getId, Node::getParentId, Node::setChildren);

        List<Node> roots = helper.treeRootAuto(nodes);
        Node root = helper.treeById(nodes, 1);

        assertEquals(1, roots.size());
        assertEquals(2, root.getChildren().size());
        assertEquals(Arrays.asList(nodes.get(1), nodes.get(2)), helper.findDirectChildrenById(nodes, 1));
        assertEquals(Arrays.asList(nodes.get(1), nodes.get(3)), helper.findAllChildrenById(nodes, 1));
        assertEquals(Arrays.asList(nodes.get(1), nodes.get(0)), helper.findAllParentById(nodes, 4));
    }

    @Test
    void shouldStopOnCyclesWhenFindingChildren() {
        List<Node> nodes = Arrays.asList(
                new Node(1, 2),
                new Node(2, 1)
        );
        TreeHelper<Node, Integer> helper = TreeHelper.of(Node::getId, Node::getParentId, Node::setChildren, 10);

        List<Node> children = helper.findAllChildrenById(nodes, 1);

        assertEquals(1, children.size());
        assertEquals(Integer.valueOf(2), children.get(0).getId());
        assertNotNull(helper.treeById(nodes, 1));
    }

    @Data
    private static class Node {
        private final Integer id;
        private final Integer parentId;
        private List<Node> children = new ArrayList<Node>();

    }
}
