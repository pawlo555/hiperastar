package org.hiperastar.examples.data;

import org.hiperastar.data.Junction;
import org.hiperastar.data.Lane;

import java.util.List;

public class Junction2D implements Junction<JunctionId, JunctionData>
{
    private final JunctionId id;
    private final JunctionData data;
    private final List<LaneId> incomingLaneIds;
    private final List<LaneId> outgoingLaneIds;


    public Junction2D(
            JunctionId id, JunctionData data,
            List<LaneId> incomingLaneIds, List<LaneId> outgoingLaneIds
    )
    {
        this.id = id;
        this.data = data;
        this.incomingLaneIds = incomingLaneIds;
        this.outgoingLaneIds = outgoingLaneIds;
    }

    @Override
    public JunctionId getID()
    {
        return id;
    }

    @Override
    public JunctionData getData() {
        return data;
    }

    public List<LaneId> getIncomingLaneIds()
    {
        return incomingLaneIds;
    }
    public List<LaneId> getOutgoingLaneIds()
    {
        return outgoingLaneIds;
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Junction2D junction = (Junction2D) o;
        return getID().equals(junction.getID());
    }
}
