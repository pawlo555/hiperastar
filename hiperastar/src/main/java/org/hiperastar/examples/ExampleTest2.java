package org.hiperastar.examples;

import org.hiperastar.data.Junction;
import org.hiperastar.data.Lane;
import org.hiperastar.data.RoadMap;
import org.hiperastar.examples.data.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExampleTest2
{
    private static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }


    public static Pair<Long, List<Double>> measure(
            List<Pair<Junction2D, Junction2D>> sourceTargetList,
            ShortestPathAlgorithm<Junction2D, Lane2D> shortestPathAlgorithm
    )
    {
        long start = System.nanoTime();
        List<GraphPath<Junction2D, Lane2D>> paths = findPaths(sourceTargetList, shortestPathAlgorithm);
        long end = System.nanoTime();

        List<Double> weightLengths = new ArrayList<>();
        for(GraphPath<Junction2D, Lane2D> path : paths)
        {
            if(path != null)
            {
                weightLengths.add(
                        path.getEdgeList()
                                .stream().map((lane) -> lane.getData().getLength())
                                .reduce(0.0, Double::sum)
                );
            }
            else
            {
                weightLengths.add(Double.POSITIVE_INFINITY);
            }
        }

        return new Pair<>(end - start, weightLengths);
    }

    private static List<Pair<Junction2D, Junction2D>> generateRandomRequests(
            Graph<Junction2D, Lane2D> graph,
            int requestCount
    )
    {
        Set<Junction2D> vertices = graph.vertexSet();
        List<Pair<Junction2D, Junction2D>> requests = new ArrayList<>();
        for(int i = 0; i < requestCount; i++)
        {
            Junction2D source = getRandomSetElement(vertices);
            Junction2D target = getRandomSetElement(vertices);
            while (target.equals(source))
                target = getRandomSetElement(vertices);
            requests.add(new Pair<>(source, target));
        }
        return requests;
    }

    public static List<GraphPath<Junction2D, Lane2D>> findPaths(
            List<Pair<Junction2D, Junction2D>> sourceTargetList,
            ShortestPathAlgorithm<Junction2D, Lane2D> shortestPathAlgorithm
    )
    {
        List<GraphPath<Junction2D, Lane2D>> paths = new ArrayList<>();
        for(Pair<Junction2D, Junction2D> sourceTarget : sourceTargetList)
        {
            paths.add(
                    shortestPathAlgorithm.getPath(sourceTarget.getFirst(), sourceTarget.getSecond())
            );
        }
        return paths;
    }

    private static RoadMap<Junction2D, Lane2D> readGraphFromFile(String file)
    {
        BufferedReader reader;

        Set<JunctionId> junctionIds = new HashSet<>();
        Map<JunctionId, JunctionData> junctionDataMapping = new HashMap<>();
        Map<Integer, JunctionId> junctionIdMapping = new HashMap<>();
        Set<Junction2D> junctions = new HashSet<>();
        Set<Lane2D> lanes = new HashSet<>();
        Map<JunctionId, Junction2D> junctionMapping = new HashMap<>();
        Map<LaneId, Lane2D> laneMapping = new HashMap<>();

        boolean readingVertices = false;
        boolean readingEdges = false;
        int currentEdge = 0;

        try {
            reader = new BufferedReader(new FileReader(file));
            for(String line = reader.readLine(); line != null; line = reader.readLine())
            {
                String[] splited = line.split("\\s+");
                if(splited.length == 1)
                {
                    if(splited[0].equals("v"))
                    {
                        readingVertices = true;
                    }
                    else if(splited[0].equals("e"))
                    {
                        readingEdges = true;
                    }
                }
                else
                {
                    if(readingEdges)
                    {
                        int id = currentEdge++;
                        LaneId lid = new LaneId(id);

                        int source = Integer.parseInt(splited[0]);
                        int target = Integer.parseInt(splited[1]);
                        double weight = Double.parseDouble(splited[2]);
                        JunctionId sourceJId = junctionIdMapping.get(source);
                        JunctionId targetJId = junctionIdMapping.get(target);

                        Lane2D lane = new Lane2D(lid, new LaneData(weight), sourceJId, targetJId);
                        lanes.add(lane);
                        laneMapping.put(lid, lane);
                    }
                    else if(readingVertices)
                    {
                        int id = Integer.parseInt(splited[0]);
                        double x = Double.parseDouble(splited[1]);
                        double y = Double.parseDouble(splited[2]);
                        JunctionId jid = new JunctionId(id);
                        junctionIds.add(jid);
                        junctionIdMapping.put(id, jid);
                        junctionDataMapping.put(jid, new JunctionData(new Vec2D(x, y)));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<JunctionId, List<LaneId>> incomingLanes = new HashMap<>();
        Map<JunctionId, List<LaneId>> outgoingLanes = new HashMap<>();

        for(JunctionId jid : junctionIds)
        {
            incomingLanes.put(jid, new ArrayList<>());
            outgoingLanes.put(jid, new ArrayList<>());
        }

        for(Lane2D lane : lanes)
        {
            incomingLanes.get(lane.getOutgoingJunctionId()).add(lane.getID());
            outgoingLanes.get(lane.getIncomingJunctionId()).add(lane.getID());
        }

        for(JunctionId jid : junctionIds)
        {
            JunctionData data = junctionDataMapping.get(jid);
            Junction2D junction = new Junction2D(jid, data, incomingLanes.get(jid), outgoingLanes.get(jid));
            junctions.add(junction);
            junctionMapping.put(jid, junction);
        }

        Junction2DAccessor jAccessor = new Junction2DAccessor(
                laneMapping
        );
        Lane2DAccessor lAccessor = new Lane2DAccessor(
                junctionMapping
        );

        return new RoadMap<>(
                junctions,
                lanes,
                jAccessor,
                lAccessor
        );
    }

    public static ShortestPathAlgorithm<Junction2D, Lane2D> initAStar(Graph<Junction2D, Lane2D> graph)
    {
        return new AStarShortestPath<>(
                graph, new AStarHeuristicForJunction2D()
        );
    }

    public static
    Pair<ContractionHierarchyPrecomputation.ContractionHierarchy<Junction2D, Lane2D>, Long> initContractionHierarchy(
            Graph<Junction2D, Lane2D> graph, ThreadPoolExecutor executor
    )
    {
        ContractionHierarchyPrecomputation<Junction2D, Lane2D> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        long start = System.nanoTime();
        ContractionHierarchyPrecomputation.ContractionHierarchy<Junction2D, Lane2D> ch =
                precomputation.computeContractionHierarchy();
        long end = System.nanoTime();

        return new Pair<>(ch, end - start);
    }

    public static ShortestPathAlgorithm<Junction2D, Lane2D> initCHDijkstra(
            ContractionHierarchyPrecomputation.ContractionHierarchy<Junction2D, Lane2D> ch
    )
    {
        return new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    public static void main(String[] args)
    {
        int requestCount = 4, threadCount = 16;

        Graph<Junction2D, Lane2D> graph = readGraphFromFile("example_graph.txt");

        for(Junction2D vertex : graph.vertexSet())
        {
            System.out.println(
                        "Vertex ID=" + vertex.getID() +
                        " degree=" + graph.degreeOf(vertex) +
                        " in degree=" + graph.inDegreeOf(vertex) +
                        " out degree=" + graph.outDegreeOf(vertex)
            );
        }
        for(Lane2D edge : graph.edgeSet())
        {
            System.out.println(
                    "Edge ID=" + edge.getID() +
                    " source=" + edge.getIncomingJunctionId().getID() +
                    " target=" + edge.getOutgoingJunctionId().getID() +
                    " weight=" + edge.getData().getLength()
            );
        }

        // Contraction Hierarchies

        System.out.println("\nCH Precomputation\n");

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);

        Pair<ContractionHierarchyPrecomputation.ContractionHierarchy<Junction2D, Lane2D>, Long> cpt =
                initContractionHierarchy(graph, executor);

        ShortestPathAlgorithm<Junction2D, Lane2D> chDijkstra = initCHDijkstra(cpt.getFirst());

        System.out.println("ContractionHierarchies precomputation time taken: " + cpt.getSecond() / 1e9 + "s");
        // A*

        ShortestPathAlgorithm<Junction2D, Lane2D> aStarAlgorithm = initAStar(graph);

        // requests

        List<Pair<Junction2D, Junction2D>> requests = generateRandomRequests(graph, requestCount);

        // A*
        System.out.println("\nA*\n");

        {
            Pair<Long, List<Double>> resultAStar = measure(requests, aStarAlgorithm);
            System.out.println("AStar time taken for (requests_count=" + requestCount + "): "
                    + resultAStar.getFirst() / 1e9 + "s");

            List<Pair<Pair<Junction2D, Junction2D>, Double>> requestResult = new ArrayList<>();
            for(int i=0;i<requests.size();i++)
            {
                requestResult.add(new Pair<>(requests.get(i), resultAStar.getSecond().get(i)));
            }

            for(Pair<Pair<Junction2D, Junction2D>, Double> res : requestResult)
            {
                int idSource = res.getFirst().getFirst().getID().getID();
                int idTarget = res.getFirst().getSecond().getID().getID();
                double getLength = res.getSecond();

                System.out.println("Path between s=" + idSource + " t=" + idTarget + ": " + getLength);
            }
        }

        // CH Djikstra
        System.out.println("\nCH Dijkstra\n");

        {
            Pair<Long, List<Double>> resultCHDijkstra = measure(requests, chDijkstra);
            System.out.println("CHDijkstra time taken for (requests_count=" + requestCount + "): "
                    + resultCHDijkstra.getFirst() / 1e9 + "s");

            List<Pair<Pair<Junction2D, Junction2D>, Double>> requestResult = new ArrayList<>();
            for(int i=0;i<requests.size();i++)
            {
                requestResult.add(new Pair<>(requests.get(i), resultCHDijkstra.getSecond().get(i)));
            }

            for(Pair<Pair<Junction2D, Junction2D>, Double> res : requestResult)
            {
                int idSource = res.getFirst().getFirst().getID().getID();
                int idTarget = res.getFirst().getSecond().getID().getID();
                double getLength = res.getSecond();

                System.out.println("Path between s=" + idSource + " t=" + idTarget + ": " + getLength);
            }
        }

        executor.shutdown();
    }
}
