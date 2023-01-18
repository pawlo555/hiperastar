package org.hiperastar.examples.data;

import org.hiperastar.data.Lane;

public class LaneId
{
    private final int id;

    public LaneId(int id)
    {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LaneId laneId = (LaneId) o;
        return id == laneId.id;
    }
}
