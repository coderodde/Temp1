package net.coderodde.graph.pathfinding.support;

import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphPath;

public final class DijkstraPathfinder extends AbstractPathfinder {

    private final AStarPathfinder finderImplementation;

    public DijkstraPathfinder(DirectedGraph graph,
                              DirectedGraphWeightFunction weightFunction) {
        this.finderImplementation = 
                new AStarPathfinder(graph, 
                                    weightFunction,
                                    (a, b) -> { return 0.0; });
    }

    @Override
    public DirectedGraphPath search(int sourceNodeId, int targetNodeId) {
        return finderImplementation.search(sourceNodeId, targetNodeId);
    }
}
