package org.hiperastar.examples.data;

import org.hiperastar.data.LaneAccessor;

import java.util.Map;

public class Lane2DAccessor implements LaneAccessor<Junction2D, Lane2D>
{
    private final Map<JunctionId, Junction2D> junctionIdJunctionMap;

    public Lane2DAccessor(Map<JunctionId, Junction2D> junctionIdJunctionMap)
    {
        this.junctionIdJunctionMap = junctionIdJunctionMap;
    }

    @Override
    public Junction2D getIncomingJunction(Lane2D lane)
    {
        return junctionIdJunctionMap.get(lane.getIncomingJunctionId());
    }

    @Override
    public Junction2D getOutgoingJunction(Lane2D lane)
    {
        return junctionIdJunctionMap.get(lane.getOutgoingJunctionId());
    }

    @Override
    public double getLaneWeight(Lane2D lane) {
        return lane.getData().getLength();
    }
}
