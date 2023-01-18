package org.hiperastar.data;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RoadMap<J extends Junction<?,?>, L extends Lane<?,?>> implements Graph<J, L>
{
    private final Set<J> junctions;
    private final Set<L> lanes;
    private final JunctionAccessor<J, L> junctionAccessor;
    private final LaneAccessor<J, L> laneAccessor;

    public RoadMap(
            Set<J> junctions,
            Set<L> lanes,
            JunctionAccessor<J, L> junctionAccessor,
            LaneAccessor<J, L> laneAccessor
    )
    {
        this.junctions = junctions;
        this.lanes = lanes;
        this.junctionAccessor = junctionAccessor;
        this.laneAccessor = laneAccessor;
    }

    @Override
    public Set<L> getAllEdges(J j0, J j1)
    {
        if(!junctions.contains(j0) || !junctions.contains(j1))
            return null;
        Set<L> lanes = junctionAccessor.getOutgoingLanes(j0);
        return lanes
                .stream()
                .filter((lane) -> laneAccessor.getOutgoingJunction(lane).equals(j1))
                .collect(Collectors.toSet());
    }

    @Override
    public L getEdge(J j0, J j1)
    {
        if(!junctions.contains(j0) || !junctions.contains(j1))
            return null;
        Set<L> lanes = junctionAccessor.getOutgoingLanes(j0);
        return lanes
                .stream()
                .filter((lane) -> laneAccessor.getOutgoingJunction(lane).equals(j1))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean containsEdge(L l)
    {
        return lanes.contains(l);
    }

    @Override
    public boolean containsEdge(J j0, J j1)
    {
        return getEdge(j0, j1) != null;
    }

    @Override
    public boolean containsVertex(J j) {
        return junctions.contains(j);
    }

    @Override
    public Set<L> edgeSet()
    {
        return lanes;
    }

    @Override
    public int degreeOf(J j)
    {
        return junctionAccessor.incomingLanesCount(j) + junctionAccessor.outgoingLanesCount(j);
    }

    @Override
    public Set<L> edgesOf(J j)
    {
        return new HashSet<L>() {
            {
                addAll(junctionAccessor.getIncomingLanes(j));
                addAll(junctionAccessor.getOutgoingLanes(j));
            }
        };
    }

    @Override
    public int inDegreeOf(J j)
    {
        return junctionAccessor.incomingLanesCount(j);
    }

    @Override
    public Set<L> incomingEdgesOf(J j)
    {
        return junctionAccessor.getIncomingLanes(j);
    }

    @Override
    public int outDegreeOf(J j)
    {
        return junctionAccessor.outgoingLanesCount(j);
    }

    @Override
    public Set<L> outgoingEdgesOf(J j)
    {
        return junctionAccessor.getOutgoingLanes(j);
    }

    @Override
    public boolean removeAllEdges(Collection<? extends L> collection) { return false; }

    @Override
    public Set<J> vertexSet()
    {
        return junctions;
    }

    @Override
    public J getEdgeSource(L l)
    {
        return laneAccessor.getIncomingJunction(l);
    }

    @Override
    public J getEdgeTarget(L l)
    {
        return laneAccessor.getOutgoingJunction(l);
    }


    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder(true, false)
                .directed()
                .allowCycles(true)
                .allowMultipleEdges(true)
                .allowSelfLoops(true)
                .modifiable(false)
                .build();
    }

    @Override
    public double getEdgeWeight(L l)
    {
        return laneAccessor.getLaneWeight(l);
    }

    // non modifiable parts
    @Override
    public void setEdgeWeight(L l, double v) { }

    @Override
    public Supplier<J> getVertexSupplier() { return null; }

    @Override
    public Supplier<L> getEdgeSupplier() { return null; }

    @Override
    public L addEdge(J j, J v1) { return null; }

    @Override
    public boolean addEdge(J j, J v1, L l) { return false; }

    @Override
    public J addVertex() { return null; }

    @Override
    public boolean addVertex(J j) { return false; }

    @Override
    public Set<L> removeAllEdges(J j, J v1) { return null; }

    @Override
    public boolean removeAllVertices(Collection<? extends J> collection) { return false; }

    @Override
    public L removeEdge(J j, J v1) { return null; }

    @Override
    public boolean removeEdge(L l) { return false; }

    @Override
    public boolean removeVertex(J j) { return false; }
}
