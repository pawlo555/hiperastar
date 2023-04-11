//package org.hiperastar;
//
//import org.jgrapht.Graph;
//import org.jgrapht.GraphPath;
//import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
//import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
//import org.jgrapht.alg.shortestpath.*;
//import org.jgrapht.alg.util.Pair;
//import org.jgrapht.graph.EdgeReversedGraph;
//import org.jgrapht.graph.GraphWalk;
//import org.jgrapht.graph.MaskSubgraph;
//import org.jheaps.AddressableHeap;
//import org.jheaps.tree.PairingHeap;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.function.Supplier;
//
//
//
//public class ContractionHierarchyAStarShortestPath<V, E> implements ShortestPathAlgorithm<V, E> {
//    private final Graph<V, E> graph;
//    private final AStarAdmissibleHeuristic<V> admissibleHeuristic;
//    private final ContractionHierarchyPrecomputation.ContractionHierarchy<V, E> contractionHierarchy;
//    private final Graph<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> contractionGraph;
//    private final Map<V, ContractionHierarchyPrecomputation.ContractionVertex<V>> contractionMapping;
//    private final Supplier<AddressableHeap<Double, Pair<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>>>> heapSupplier;
//    private final double radius;
//
//    public ContractionHierarchyAStarShortestPath(
//            ContractionHierarchyPrecomputation.ContractionHierarchy<V, E> hierarchy,
//            AStarAdmissibleHeuristic<V> admissibleHeuristic
//    ) {
//        this(hierarchy, admissibleHeuristic, Double.POSITIVE_INFINITY, PairingHeap::new);
//    }
//
//    public ContractionHierarchyAStarShortestPath(
//            ContractionHierarchyPrecomputation.ContractionHierarchy<V, E> hierarchy,
//            AStarAdmissibleHeuristic<V> admissibleHeuristic,
//            double radius,
//            Supplier<AddressableHeap<Double, Pair<ContractionHierarchyPrecomputation.ContractionVertex<V>,
//            ContractionHierarchyPrecomputation.ContractionEdge<E>>>> heapSupplier
//    ) {
//        this.graph = hierarchy.getGraph();
//        this.admissibleHeuristic = admissibleHeuristic;
//        this.contractionHierarchy = hierarchy;
//        this.contractionGraph = hierarchy.getContractionGraph();
//        this.contractionMapping = hierarchy.getContractionMapping();
//        this.radius = radius;
//        this.heapSupplier = heapSupplier;
//    }
//
//    private GraphPath<V, E> createEmptyPath(V source, V sink) {
//        return source.equals(sink) ? GraphWalk.singletonWalk(this.graph, source, 0.0) : null;
//    }
//
//    @Override
//    public GraphPath<V, E> getPath(V source, V sink) {
//        if (!this.graph.containsVertex(source)) {
//            throw new IllegalArgumentException("Graph must contain the source vertex!");
//        } else if (!this.graph.containsVertex(sink)) {
//            throw new IllegalArgumentException("Graph must contain the sink vertex!");
//        } else if (source.equals(sink)) {
//            return this.createEmptyPath(source, sink);
//        } else {
//            ContractionHierarchyPrecomputation.ContractionVertex<V> contractedSource = (ContractionHierarchyPrecomputation.ContractionVertex)this.contractionMapping.get(source);
//            ContractionHierarchyPrecomputation.ContractionVertex<V> contractedSink = (ContractionHierarchyPrecomputation.ContractionVertex)this.contractionMapping.get(sink);
//            ContractionHierarchyAStarShortestPath.ContractionSearchFrontier<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> forwardFrontier = new ContractionHierarchyBidirectionalDijkstra.ContractionSearchFrontier(new MaskSubgraph(this.contractionGraph, (vx) -> {
//                return false;
//            }, (ex) -> {
//                return !ex.isUpward;
//            }), this.heapSupplier);
//            ContractionHierarchyAStarShortestPath.ContractionSearchFrontier<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> backwardFrontier = new ContractionHierarchyBidirectionalDijkstra.ContractionSearchFrontier(new MaskSubgraph(new EdgeReversedGraph(this.contractionGraph), (vx) -> {
//                return false;
//            }, (ex) -> {
//                return ex.isUpward;
//            }), this.heapSupplier);
//            forwardFrontier.updateDistance(contractedSource, (Object)null, 0.0);
//            backwardFrontier.updateDistance(contractedSink, (Object)null, 0.0);
//            double bestPath = Double.POSITIVE_INFINITY;
//            ContractionHierarchyPrecomputation.ContractionVertex<V> bestPathCommonVertex = null;
//            ContractionHierarchyAStarShortestPath.ContractionSearchFrontier<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> frontier = forwardFrontier;
//            ContractionHierarchyAStarShortestPath.ContractionSearchFrontier<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> otherFrontier = backwardFrontier;
//
//            while(true) {
//                if (frontier.heap.isEmpty()) {
//                    frontier.isFinished = true;
//                }
//
//                if (otherFrontier.heap.isEmpty()) {
//                    otherFrontier.isFinished = true;
//                }
//
//                if (frontier.isFinished && otherFrontier.isFinished) {
//                    if (Double.isFinite(bestPath) && bestPath <= this.radius) {
//                        return this.createPath(forwardFrontier, backwardFrontier, bestPath, contractedSource, bestPathCommonVertex, contractedSink);
//                    }
//
//                    return this.createEmptyPath(source, sink);
//                }
//
//                if ((Double)frontier.heap.findMin().getKey() >= bestPath) {
//                    frontier.isFinished = true;
//                } else {
//                    AddressableHeap.Handle<Double, Pair<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>>> node = frontier.heap.deleteMin();
//                    ContractionHierarchyPrecomputation.ContractionVertex<V> v = (ContractionHierarchyPrecomputation.ContractionVertex)((Pair)node.getValue()).getFirst();
//                    double vDistance = (Double)node.getKey();
//                    Iterator var16 = frontier.graph.outgoingEdgesOf(v).iterator();
//
//                    while(var16.hasNext()) {
//                        ContractionHierarchyPrecomputation.ContractionEdge<E> e = (ContractionHierarchyPrecomputation.ContractionEdge)var16.next();
//                        ContractionHierarchyPrecomputation.ContractionVertex<V> u = (ContractionHierarchyPrecomputation.ContractionVertex)frontier.graph.getEdgeTarget(e);
//                        double eWeight = frontier.graph.getEdgeWeight(e);
//                        frontier.updateDistance(u, e, vDistance + eWeight);
//                        double pathDistance = vDistance + eWeight + otherFrontier.getDistance(u);
//                        if (pathDistance < bestPath) {
//                            bestPath = pathDistance;
//                            bestPathCommonVertex = u;
//                        }
//                    }
//                }
//
//                if (!otherFrontier.isFinished) {
//                    ContractionHierarchyAStarShortestPath.ContractionSearchFrontier<ContractionHierarchyPrecomputation.ContractionVertex<V>, ContractionHierarchyPrecomputation.ContractionEdge<E>> tmpFrontier = frontier;
//                    frontier = otherFrontier;
//                    otherFrontier = tmpFrontier;
//                }
//            }
//        }
//    }
//
//    @Override
//    public SingleSourcePaths<V, E> getPaths(V source) {
//        if(!this.graph.containsVertex(source))
//        {
//            throw new IllegalArgumentException("graph must contain the source vertex");
//        } else {
//            Map<V, GraphPath<V, E>> paths = new HashMap<>();
//            for(V v : this.graph.vertexSet())
//            {
//                paths.put(v, this.getPath(source, v));
//            }
//            return new ListSingleSourcePathsImpl<>(this.graph, source, paths);
//        }
//    }
//
//    @Override
//    public double getPathWeight(V v0, V v1) {
//        GraphPath<V, E> path = this.getPath(v0, v1);
//        return path == null ? Double.POSITIVE_INFINITY : path.getWeight();
//    }
//
//}
