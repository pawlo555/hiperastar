package org.hiperastar;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

public class AStarHeuristicForNode implements AStarAdmissibleHeuristic<Node> {
    @Override
    public double getCostEstimate(Node sourceVertex, Node targetVertex) {
        double diffX = sourceVertex.getX() - targetVertex.getX();
        double diffY = sourceVertex.getY() - targetVertex.getY();
        double distanceSquared = diffX * diffX + diffY * diffY;
        return Math.sqrt(distanceSquared);
    }

    @Override
    public <E> boolean isConsistent(Graph<Node, E> graph) {
        return AStarAdmissibleHeuristic.super.isConsistent(graph);
    }
}
