package net.coderodde.graph.pathfinding.support;

import net.coderodde.graph.pathfinding.HeuristicFunction;

public class ZeroHeuristicFunction implements HeuristicFunction {

    @Override
    public double estimateDistanceBetween(int nodeId1, int nodeId2) {
        return 0.0;
    }
}
