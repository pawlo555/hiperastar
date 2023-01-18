package org.hiperastar.examples;

import org.hiperastar.examples.data.Node;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

public class AStarHeuristicForNode implements AStarAdmissibleHeuristic<Node> {
    @Override
    public double getCostEstimate(Node sourceVertex, Node targetVertex) {
        double diffX = sourceVertex.getX() - targetVertex.getX();
        double diffY = sourceVertex.getY() - targetVertex.getY();
        double distanceSquared = diffX*diffX + diffY*diffY;
        return distanceSquared;
    }

    @Override
    public <E> boolean isConsistent(Graph<Node, E> graph) {
        return AStarAdmissibleHeuristic.super.isConsistent(graph);
    }
}
