package org.hiperastar.data;

import java.util.Set;

public interface JunctionAccessor<J extends Junction<?, ?>, L extends Lane<?, ?>>
{
    public Set<L> getIncomingLanes(J junction);
    public Set<L> getOutgoingLanes(J junction);

    public int incomingLanesCount(J junction);
    public int outgoingLanesCount(J junction);
}
