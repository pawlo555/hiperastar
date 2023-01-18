# HiperAStar

Small project with purpose of simplifying and speeding up calculations of shortest paths for large road networks.


## Usage
* Project assumes that road network data is split into sets of [Junctions](hiperastar/src/main/java/org/hiperastar/data/Junction.java) and [Lanes](hiperastar/src/main/java/org/hiperastar/data/Lane.java), it assumes that Lanes are one-directional (obviously - but nobody knows what's the use-case) and Junctions to allow multiple lanes between them (Multigraph) 
* Implementation of those interface should only contain data necessary for identification of a Junction/Lane and values that can be converted in user defined way to positional data (position on a road map i.e latitude longitude or just x,y) for Junctions and length-like value for Lanes
* Then there is topic of RoadMap (graph object) which also requirest implementation of [JunctionAccessor](hiperastar/src/main/java/org/hiperastar/data/JunctionAccessor.java) and [LaneAccessor](hiperastar/src/main/java/org/hiperastar/data/LaneAccessor.java) interfaces. Their purpose is to remove responsibility of holding lanes going in and out of a Junction and source/target Junctions of a Lane. They possess that information and are necessary for correct working of RoadMap object.

## Serialization and Deserialization

JGraphT library supports Serialization for all types of graphs (assuming that the data within them is just bare data) thus serialization of RoadMap and eg. ContractionGraph based on it is possible.

## References

* [JGRaphT](https://jgrapht.org/) - RoadMap implements Graph interface JGraphT allowing to run complex algorithms supplied by this library

## Example

Repository contains two example files:
* [Example1](hiperastar/src/main/java/org/hiperastar/examples/ExampleTest1.java) - Performance tests on bare JGraphT structures
* [Example2](hiperastar/src/main/java/org/hiperastar/examples/ExampleTest2.java) - Full example showing how should the interfaces be implemented (Junction is placed on grid map - x,y positional data, Lane is just providing normal double length), additionally it measures the performance of several shortest-path methods (like ContractionGraph Djikstra and regular AStar)
