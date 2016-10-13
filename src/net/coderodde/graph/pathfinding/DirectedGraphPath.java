package net.coderodde.graph.pathfinding;

import java.util.ArrayList;
import java.util.List;
import net.coderodde.graph.DirectedGraphWeightFunction;

public final class DirectedGraphPath {

    private final List<Integer> path;
    
    public DirectedGraphPath(List<Integer> path) {
        this.path = new ArrayList<>(path);
    }
    
    public int getNode(int index) {
        return path.get(index);
    }
    
    public double getCost(DirectedGraphWeightFunction weightFunction) {
        double cost = 0.0;
        
        for (int i = 0; i < path.size() - 1; ++i) {
            cost += weightFunction.get(path.get(i), path.get(i + 1));
        }
        
        return cost;
    }
        
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        
        return path.equals(((DirectedGraphPath) o).path);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        String separator = "";
        
        for (Integer node : path) {
            sb.append(separator).append(node);
            separator = ", ";
        }
        
        return sb.append(']').toString();
    }
}
