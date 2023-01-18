package org.hiperastar.examples.data;

public class JunctionId
{
    private final int id;

    public JunctionId(int id)
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
        JunctionId junctionId = (JunctionId) o;
        return id == junctionId.id;
    }
}
