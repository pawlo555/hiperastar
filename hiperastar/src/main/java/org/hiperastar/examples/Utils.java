package org.hiperastar.examples;

import org.hiperastar.contraction_hierarchies_astar.ContractionHierarchyAstar;
import org.hiperastar.contraction_hierarchies_astar.CustomContractionHierarchyPrecomputation;
import org.hiperastar.examples.data.Node;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.util.SupplierUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {

    public static Pair<CustomContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge>, Long> initContractionHierarchy(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor) {
        CustomContractionHierarchyPrecomputation<Node, DefaultWeightedEdge> precomputation =
                new CustomContractionHierarchyPrecomputation<>(graph, executor);

        long start = System.nanoTime();
        CustomContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch =
                precomputation.computeContractionHierarchy();
        long end = System.nanoTime();

        return new Pair<>(ch, end - start);
    }

    private static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }

    public static Pair<Long, List<Double>> measure(
            List<Pair<Node, Node>> sourceTargetList,
            ShortestPathAlgorithm<Node, DefaultWeightedEdge> shortestPathAlgorithm) {
        long start = System.nanoTime();
        List<GraphPath<Node, DefaultWeightedEdge>> paths = findPaths(sourceTargetList, shortestPathAlgorithm);
        long end = System.nanoTime();

        List<Double> weightLengths = new ArrayList<>();
        for(GraphPath<Node, DefaultWeightedEdge> path : paths) {
            weightLengths.add(path.getWeight());
        }

        return new Pair<>(end - start, weightLengths);
    }

    public static List<GraphPath<Node, DefaultWeightedEdge>> findPaths(
            List<Pair<Node, Node>> sourceTargetList,
            ShortestPathAlgorithm<Node, DefaultWeightedEdge> shortestPathAlgorithm) {
        List<GraphPath<Node, DefaultWeightedEdge>> paths = new ArrayList<>();
        for(Pair<Node, Node> sourceTarget : sourceTargetList) {
            paths.add(
                    shortestPathAlgorithm.getPath(sourceTarget.getFirst(), sourceTarget.getSecond())
            );

        }
        return paths;
    }

    public static List<Pair<Node, Node>> generateRandomRequests(
            Graph<Node, DefaultWeightedEdge> graph,
            int requestCount) {
        Set<Node> vertices = graph.vertexSet();
        List<Pair<Node, Node>> requests = new ArrayList<>();
        for(int i = 0; i < requestCount; i++) {
            requests.add(new Pair<>(getRandomSetElement(vertices), getRandomSetElement(vertices)));
        }
        return requests;
    }

    public static Pair<Graph<Node, DefaultWeightedEdge>, Long> generateGrid(int rows, int cols) {
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

        long start = System.nanoTime();

        generator.generateGraph(graph);

        Set<DefaultWeightedEdge> edges = graph.edgeSet();

        for(DefaultWeightedEdge edge : edges) {
            graph.setEdgeWeight(edge, Math.random() * (10 - 1) + 1);
        }

        long end = System.nanoTime();

        return new Pair<>(graph, end - start);
    }

    public static ShortestPathAlgorithm<Node, DefaultWeightedEdge> initAStar(Graph<Node, DefaultWeightedEdge> graph) {
        return new AStarShortestPath<>(
                graph, new AStarHeuristicForNode()
        );
    }

    public static ShortestPathAlgorithm<Node, DefaultWeightedEdge> initCHDijkstra(
            CustomContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch) {
        return new ContractionHierarchyAstar<>(ch);
    }
}
