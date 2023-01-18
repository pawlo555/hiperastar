package org.hiperastar.examples;

import org.hiperastar.examples.data.Junction2D;
import org.hiperastar.examples.data.Vec2D;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

public class AStarHeuristicForJunction2D implements AStarAdmissibleHeuristic<Junction2D> {
    @Override
    public double getCostEstimate(Junction2D sourceVertex, Junction2D targetVertex) {
        Vec2D posSource = sourceVertex.getData().getPosition();
        Vec2D posTarget = targetVertex.getData().getPosition();
        return posSource.subtract(posTarget).length();
    }

    @Override
    public <E> boolean isConsistent(Graph<Junction2D, E> graph) {
        return AStarAdmissibleHeuristic.super.isConsistent(graph);
    }
}
