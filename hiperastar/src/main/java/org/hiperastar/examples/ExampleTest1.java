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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class ExampleTest1 {
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

    private static List<Pair<Node, Node>> generateRandomRequests(
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

    public static
    Pair<ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge>, Long> initContractionHierarchy(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor) {
        ContractionHierarchyPrecomputation<Node, DefaultWeightedEdge> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        long start = System.nanoTime();
        ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch =
                precomputation.computeContractionHierarchy();
        long end = System.nanoTime();

        return new Pair<>(ch, end - start);
    }

    public static ShortestPathAlgorithm<Node, DefaultWeightedEdge> initCHDijkstra(
            ContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge> ch) {
        return new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    public static Pair<ShortestPathAlgorithm<Node, DefaultWeightedEdge>, Long> initTransitNodeRouting(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor) {
        TransitNodeRoutingShortestPath<Node, DefaultWeightedEdge> tnr
                = new TransitNodeRoutingShortestPath<>(graph, executor);
        long start = System.nanoTime();
        tnr.performPrecomputation();
        long end = System.nanoTime();

        return new Pair<>(tnr, end - start);
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

    public static void experiment(int rows, int cols, int requestCount, int threadCount) {
        System.out.println("#######" + rows + ":" + threadCount + "#######");
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
        chCreate.add(cpt.getSecond() / 1e9);

        // TransitNodeRouting

//        Pair<ShortestPathAlgorithm<Node, DefaultWeightedEdge>, Long> tnrt
//                = initTransitNodeRouting(graph, executor);
//
//        ShortestPathAlgorithm<Node, DefaultWeightedEdge> transitNodeRouting = tnrt.getFirst();
//
//        System.out.println("TransitNodeRouting precomputation time taken: " + tnrt.getSecond() / 1e9 + "s");
//        tnrCreate.add(tnrt.getSecond() / 1e9);

        // requests

        List<Pair<Node, Node>> requests = generateRandomRequests(graph, requestCount);

        Pair<Long, List<Double>> resultAStar = measure(requests, aStarAlgorithm);
        System.out.println("AStar time taken for (requests_count=" + requestCount + "): "
                + resultAStar.getFirst() / 1e9 + "s");
        aStarTimes.add(resultAStar.getFirst() / 1e9);

        Pair<Long, List<Double>> resultCHDijkstra = measure(requests, chDijkstra);
        System.out.println("CHDijkstra time taken for (requests_count=" + requestCount + "): "
                + resultCHDijkstra.getFirst() / 1e9 + "s");
        chTimes.add(resultCHDijkstra.getFirst() / 1e9);

        //Pair<Long, List<Double>> resultTransitNodeRouting = measure(requests, transitNodeRouting);
        //System.out.println("TransitNodeRouting time taken for (requests_count=" + requestCount + "): "
        //        + resultTransitNodeRouting.getFirst() / 1e9 + "s");
        //tnrTimes.add(resultTransitNodeRouting.getFirst() / 1e9);

        executor.shutdown();
    }

    public static ArrayList<Double> aStarTimes = new ArrayList<>();
    public static ArrayList<Double> chTimes = new ArrayList<>();
    //public static ArrayList<Double> tnrTimes = new ArrayList<>();
    public static ArrayList<Double> chCreate = new ArrayList<>();
    //public static ArrayList<Double> tnrCreate = new ArrayList<>();

    public static List<Integer> sizesList = Arrays.asList(10, 20);
    public static List<Integer> threadsCountList = Arrays.asList(6, 12);
    public static int requestCount = 10000;

    public static void main(String[] args) {
        for (int threadsCount: threadsCountList) {
            for (int size: sizesList) {
                experiment(size, size, requestCount, threadsCount);
            }
        }

        System.out.println("AStar times:" + aStarTimes);
        System.out.println("CH times:" + chTimes);
        //System.out.println("TNR times:" + tnrTimes);
        System.out.println("CH create times:" + chCreate);
        //System.out.println("TNR create times:" + tnrCreate);

        //exportAsPNG();
    }

    public static void saveChart(JFreeChart chart, String name) {
        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
        Rectangle r = new Rectangle(0, 0, 600, 400);
        chart.draw(g2, r);
        File f = new File(name);

        BufferedImage chartImage = chart.createBufferedImage( 600, 400, null);
        try {
            ImageIO.write( chartImage, "png", f );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportAsPNG() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int tIdx=0; tIdx<threadsCountList.size(); tIdx++) {
            XYSeries seriesAStar = new XYSeries("AStar-" + threadsCountList.get(tIdx));
            XYSeries seriesCH = new XYSeries("CH-" + threadsCountList.get(tIdx));
            //XYSeries seriesTNR = new XYSeries("TNR-" + threadsCountList.get(tIdx));
            for (int sIdx=0; sIdx<sizesList.size(); sIdx++) {
                int size = sizesList.get(sIdx)*sizesList.get(sIdx);
                System.out.println(tIdx*sizesList.size() + sIdx);
                System.out.println(size + ", " +  tIdx*sizesList.size() + ", " + aStarTimes.get(sIdx+tIdx*sizesList.size()));
                seriesAStar.add(size, aStarTimes.get(sIdx+tIdx*sizesList.size()));
                seriesCH.add(size, chTimes.get(sIdx+tIdx*sizesList.size()));
                //seriesTNR.add(size, tnrTimes.get(sIdx+tIdx*sizesList.size()));
            }
            dataset.addSeries(seriesAStar);
            dataset.addSeries(seriesCH);
            //dataset.addSeries(seriesTNR);
        }

        JFreeChart requestChart = ChartFactory.createXYLineChart(
                "Requests execution time",
                "Number of vertexes",
                "Time of execution for random 10000 requests [s]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        saveChart(requestChart, "requests.png");

        dataset = new XYSeriesCollection();

        for (int tIdx=0; tIdx<threadsCountList.size(); tIdx++) {
            XYSeries seriesCH = new XYSeries("CH-" + threadsCountList.get(tIdx));
            //XYSeries seriesTNR = new XYSeries("TNR-" + threadsCountList.get(tIdx));
            for (int sIdx=0; sIdx<sizesList.size(); sIdx++) {
                int size = sizesList.get(sIdx)*sizesList.get(sIdx);
                seriesCH.add(size, chCreate.get(sIdx+tIdx*sizesList.size()));
                //seriesTNR.add(size, tnrCreate.get(sIdx+tIdx*sizesList.size()));
            }

            dataset.addSeries(seriesCH);
            //dataset.addSeries(seriesTNR);
        }

        JFreeChart constructionChart = ChartFactory.createXYLineChart(
                "Time of preparing graph",
                "Number of vertexes",
                "Time of execution for random 10000 requests [s]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        saveChart(constructionChart, "preprocessing.png");
    }
}
