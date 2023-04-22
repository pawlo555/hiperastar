import org.hiperastar.contraction_hierarchies_astar.ContractionHierarchyAstar;
import org.hiperastar.contraction_hierarchies_astar.CustomContractionHierarchyPrecomputation;

import org.hiperastar.examples.data.Node;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;


import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;


public class TestCHAStar {

    private Graph<Node, DefaultWeightedEdge> createGraph(int rows, int cols) {
        AtomicReference<Integer> i = new AtomicReference<>(0);
        DefaultDirectedGraph<Node, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(
                () -> {
                    int x = i.get() / rows;
                    int y = (i.get() % rows);
                    Node node = new Node(x, y);
                    i.set(i.get() + 1);
                    return node;
                },
                SupplierUtil.createDefaultWeightedEdgeSupplier(),
                true
        );

        GraphGenerator<Node, DefaultWeightedEdge, Node> generator = new GridGraphGenerator<>(rows, cols);
        generator.generateGraph(graph);

        Set<DefaultWeightedEdge> edges = graph.edgeSet();
        for(DefaultWeightedEdge edge : edges) {
            graph.setEdgeWeight(edge, Math.random() * (10 - 1) + 1);
        }

        return graph;
    }

    private ShortestPathAlgorithm<Node, DefaultWeightedEdge> createCHBidirectionalDijkstra(
            Graph<Node, DefaultWeightedEdge> graph) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        ContractionHierarchyPrecomputation<Node, DefaultWeightedEdge> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch =
                precomputation.computeContractionHierarchy();

        return new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    private ShortestPathAlgorithm<Node, DefaultWeightedEdge> createCHAStar(
            Graph<Node, DefaultWeightedEdge> graph) {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        CustomContractionHierarchyPrecomputation<Node, DefaultWeightedEdge> precomputation =
                new CustomContractionHierarchyPrecomputation<>(graph, executor);

        CustomContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch =
                precomputation.computeContractionHierarchy();

        return new ContractionHierarchyAstar<>(ch);
    }

    @Test
    public void testAddingVariables() {
        Graph<Node, DefaultWeightedEdge> graph = createGraph(4, 4);

        Node source = null;
        Node sink = null;

        for (Node node: graph.vertexSet()) {
            if (node.getX() == 0 && node.getY() == 0) {
                source = node;
            }
            if (node.getX() == 3 && node.getY() == 3) {
                sink = node;
            }
        }


        ShortestPathAlgorithm<Node, DefaultWeightedEdge> chBidirectionalDijkstra = createCHBidirectionalDijkstra(graph);
        ShortestPathAlgorithm<Node, DefaultWeightedEdge> chAStar = createCHAStar(graph);

        GraphPath<Node, DefaultWeightedEdge> properPath = chBidirectionalDijkstra.getPath(source, sink);
        GraphPath<Node, DefaultWeightedEdge> customPath = chAStar.getPath(source, sink);

        assertEquals(properPath.getLength(), customPath.getLength());
        System.out.println(properPath.getLength());

        for (int i=0; i<properPath.getLength(); i++) {
            System.out.println(properPath.getVertexList().get(i) + " - " + customPath.getVertexList().get(i));
            assertEquals(properPath.getVertexList().get(i), customPath.getVertexList().get(i));
        }

    }
}
