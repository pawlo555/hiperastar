package org.hiperastar.examples;

import org.hiperastar.examples.data.Node;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.shortestpath.TransitNodeRoutingShortestPath;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class ExampleTest1
{
    private static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }

    public static Pair<Long, List<Double>> measure(
            List<Pair<Node, Node>> sourceTargetList,
            ShortestPathAlgorithm<Node, DefaultWeightedEdge> shortestPathAlgorithm
    )
    {
        long start = System.nanoTime();
        List<GraphPath<Node, DefaultWeightedEdge>> paths = findPaths(sourceTargetList, shortestPathAlgorithm);
        long end = System.nanoTime();

        List<Double> weightLengths = new ArrayList<>();
        for(GraphPath<Node, DefaultWeightedEdge> path : paths)
        {
            weightLengths.add(path.getWeight());
        }

        return new Pair<>(end - start, weightLengths);
    }

    private static List<Pair<Node, Node>> generateRandomRequests(
            Graph<Node, DefaultWeightedEdge> graph,
            int requestCount
    )
    {
        Set<Node> vertices = graph.vertexSet();
        List<Pair<Node, Node>> requests = new ArrayList<>();
        for(int i = 0; i < requestCount; i++)
        {
            requests.add(new Pair<>(getRandomSetElement(vertices),getRandomSetElement(vertices)));
        }
        return requests;
    }

    public static Pair<Graph<Node, DefaultWeightedEdge>, Long> generateGrid(int rows, int cols)
    {
        AtomicReference<Integer> i = new AtomicReference<>(0);

        DefaultDirectedGraph<Node, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(
                () -> {
                    double x = (double)(i.get() / rows);
                    double y = (double)(i.get() % rows);
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

        for(DefaultWeightedEdge edge : edges)
        {
            graph.setEdgeWeight(edge, Math.random() * (10 - 1) + 1);
        }

        long end = System.nanoTime();

        return new Pair<>(graph, end - start);
    }

    public static ShortestPathAlgorithm<Node, DefaultWeightedEdge> initAStar(Graph<Node, DefaultWeightedEdge> graph)
    {
        return new AStarShortestPath<>(
                graph, new AStarHeuristicForNode()
        );
    }

    public static
    Pair<ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge>, Long> initContractionHierarchy(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor
    )
    {
        ContractionHierarchyPrecomputation<Node, DefaultWeightedEdge> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        long start = System.nanoTime();
        ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch =
                precomputation.computeContractionHierarchy();
        long end = System.nanoTime();

        return new Pair<>(ch, end - start);
    }

    public static ShortestPathAlgorithm<Node, DefaultWeightedEdge> initCHDijkstra(
            ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch
    )
    {
        return new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    public static Pair<ShortestPathAlgorithm<Node, DefaultWeightedEdge>, Long> initTransitNodeRouting(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor
    )
    {
        TransitNodeRoutingShortestPath<Node, DefaultWeightedEdge> tnr
                = new TransitNodeRoutingShortestPath<>(graph, executor);
        long start = System.nanoTime();
        tnr.performPrecomputation();
        long end = System.nanoTime();

        return new Pair<>(tnr, end - start);
    }

    public static List<GraphPath<Node, DefaultWeightedEdge>> findPaths(
            List<Pair<Node, Node>> sourceTargetList,
            ShortestPathAlgorithm<Node, DefaultWeightedEdge> shortestPathAlgorithm
    )
    {
        List<GraphPath<Node, DefaultWeightedEdge>> paths = new ArrayList<>();
        for(Pair<Node, Node> sourceTarget : sourceTargetList)
        {
            paths.add(
                    shortestPathAlgorithm.getPath(sourceTarget.getFirst(), sourceTarget.getSecond())
            );
        }
        return paths;
    }

    public static void main(String[] args)
    {
        int rows = 400, cols = 400, requestCount = 256, threadCount = 16;

        Pair<Graph<Node, DefaultWeightedEdge>, Long> gpt = generateGrid(rows, cols);

        Graph<Node, DefaultWeightedEdge> graph = gpt.getFirst();

        System.out.println("Graph generated time taken: " + gpt.getSecond() / 1e9 + "s");

        // A*

        ShortestPathAlgorithm<Node, DefaultWeightedEdge> aStarAlgorithm = initAStar(graph);

        // ContractionHierarchies

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

        Pair<ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge>, Long> cpt =
                initContractionHierarchy(graph, executor);

        ShortestPathAlgorithm<Node, DefaultWeightedEdge> chDijkstra = initCHDijkstra(cpt.getFirst());

        System.out.println("ContractionHierarchies precomputation time taken: " + cpt.getSecond() / 1e9 + "s");

        // TransitNodeRouting

        //Pair<ShortestPathAlgorithm<Node, DefaultWeightedEdge>, Long> tnrt
        //        = initTransitNodeRouting(graph, executor);

        //ShortestPathAlgorithm<Node, DefaultWeightedEdge> transitNodeRouting = tnrt.getFirst();

        //System.out.println("TransitNodeRouting precomputation time taken: " + tnrt.getSecond() / 1e9 + "s");

        // requests

        List<Pair<Node, Node>> requests = generateRandomRequests(graph, requestCount);

        Pair<Long, List<Double>> resultAStar = measure(requests, aStarAlgorithm);
        System.out.println("AStar time taken for (requests_count=" + requestCount + "): "
                + resultAStar.getFirst() / 1e9 + "s");

        Pair<Long, List<Double>> resultCHDijkstra = measure(requests, chDijkstra);
        System.out.println("CHDijkstra time taken for (requests_count=" + requestCount + "): "
                + resultCHDijkstra.getFirst() / 1e9 + "s");

        //Pair<Long, List<Double>> resultTransitNodeRouting = measure(requests, transitNodeRouting);
        //System.out.println("TransitNodeRouting time taken for (requests_count=" + requestCount + "): "
        //        + resultTransitNodeRouting.getFirst() / 1e9 + "s");

        executor.shutdown();
    }
}
