package org.hiperastar.examples.data;

import org.hiperastar.data.JunctionAccessor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Junction2DAccessor implements JunctionAccessor<Junction2D, Lane2D>
{
    private final Map<LaneId, Lane2D> laneIdToLaneMap;

    public Junction2DAccessor(Map<LaneId, Lane2D> laneIdToLaneMap)
    {
        this.laneIdToLaneMap = laneIdToLaneMap;
    }

    @Override
    public Set<Lane2D> getIncomingLanes(Junction2D junction) {
        return junction.getIncomingLaneIds().stream().map(laneIdToLaneMap::get).collect(Collectors.toSet());
    }

    @Override
    public Set<Lane2D> getOutgoingLanes(Junction2D junction) {
        return junction.getOutgoingLaneIds().stream().map(laneIdToLaneMap::get).collect(Collectors.toSet());
    }

    @Override
    public int incomingLanesCount(Junction2D junction) {
        return junction.getIncomingLaneIds().size();
    }

    @Override
    public int outgoingLanesCount(Junction2D junction) {
        return junction.getOutgoingLaneIds().size();
    }
}
