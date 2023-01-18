package org.hiperastar.examples.data;

import org.hiperastar.data.Lane;

public class Lane2D implements Lane<LaneId, LaneData>
{
    private final LaneId id;
    private final LaneData data;
    private final JunctionId incomingJunctionId;
    private final JunctionId outgoingJunctionId;

    public Lane2D(
            LaneId id, LaneData data,
            JunctionId incomingJunctionId,
            JunctionId outgoingJunctionId
    )
    {
        this.id = id;
        this.data = data;
        this.incomingJunctionId = incomingJunctionId;
        this.outgoingJunctionId = outgoingJunctionId;
    }

    @Override
    public LaneId getID()
    {
        return id;
    }

    @Override
    public LaneData getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    public JunctionId getIncomingJunctionId()
    {
        return incomingJunctionId;
    }

    public JunctionId getOutgoingJunctionId()
    {
        return outgoingJunctionId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Lane2D lane = (Lane2D) o;
        return getID().equals(lane.getID());
    }
}
