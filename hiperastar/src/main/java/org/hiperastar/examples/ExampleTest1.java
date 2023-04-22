package org.hiperastar.examples;

import org.hiperastar.examples.data.Node;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.hiperastar.contraction_hierarchies_astar.CustomContractionHierarchyPrecomputation;
import org.jgrapht.alg.shortestpath.TransitNodeRoutingShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;



public class ExampleTest1 {


    public static Pair<ShortestPathAlgorithm<Node, DefaultWeightedEdge>, Long> initTransitNodeRouting(
            Graph<Node, DefaultWeightedEdge> graph, ThreadPoolExecutor executor) {
        TransitNodeRoutingShortestPath<Node, DefaultWeightedEdge> tnr
                = new TransitNodeRoutingShortestPath<>(graph, executor);
        long start = System.nanoTime();
        tnr.performPrecomputation();
        long end = System.nanoTime();

        return new Pair<>(tnr, end - start);
    }



    public static void experiment(int rows, int cols, int requestCount, int threadCount) {
        System.out.println("#######" + rows + ":" + threadCount + "#######");
        Pair<Graph<Node, DefaultWeightedEdge>, Long> gpt = Utils.generateGrid(rows, cols);

        Graph<Node, DefaultWeightedEdge> graph = gpt.getFirst();

        System.out.println("Graph generated time taken: " + gpt.getSecond() / 1e9 + "s");

        // A*

        ShortestPathAlgorithm<Node, DefaultWeightedEdge> aStarAlgorithm = Utils.initAStar(graph);

        // ContractionHierarchies

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

        Pair<CustomContractionHierarchyPrecomputation.ContractionHierarchy<Node, DefaultWeightedEdge>, Long> cpt =
                Utils.initContractionHierarchy(graph, executor);

        ShortestPathAlgorithm<Node, DefaultWeightedEdge> chDijkstra = Utils.initCHDijkstra(cpt.getFirst());

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

        List<Pair<Node, Node>> requests = Utils.generateRandomRequests(graph, requestCount);

        Pair<Long, List<Double>> resultAStar = Utils.measure(requests, aStarAlgorithm);
        System.out.println("AStar time taken for (requests_count=" + requestCount + "): "
                + resultAStar.getFirst() / 1e9 + "s");
        aStarTimes.add(resultAStar.getFirst() / 1e9);

        Pair<Long, List<Double>> resultCHDijkstra = Utils.measure(requests, chDijkstra);
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
