package org.hiperastar.data;

public interface LaneAccessor<J extends Junction<?, ?>, L extends Lane<?, ?>>
{
    J getIncomingJunction(L lane);
    J getOutgoingJunction(L lane);

    double getLaneWeight(L lane);
}
