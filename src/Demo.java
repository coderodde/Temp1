import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphNodeCoordinates;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.support.AStarPathfinder;
import net.coderodde.graph.pathfinding.support.DijkstraPathfinder;
import net.coderodde.graph.pathfinding.support.EuclideanHeuristicFunction;

public class Demo {

    private static final int NODES = 50_000;
    private static final int ARCS = 5_000_000;
    
    public static void main(String[] args) {
        long seed = 1306862436431866L; //System.nanoTime();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);
        
        DirectedGraph graph = getRandomGraph(NODES, ARCS, random);
        DirectedGraphNodeCoordinates coordinates = getCoordinates(graph, 
                                                                  random);
        DirectedGraphWeightFunction weightFunction = 
                getWeightFunction(graph, coordinates);
        
        List<Integer> graphNodeList = new ArrayList<>(graph.getNodeList());
        
        Integer sourceNodeId = choose(graphNodeList, random);
        Integer targetNodeId = choose(graphNodeList, random);
        
        System.out.println("Source: " + sourceNodeId);
        System.out.println("Target: " + targetNodeId);
        
        System.out.println();
        
        HeuristicFunction hf = new EuclideanHeuristicFunction(coordinates);
        
        AbstractPathfinder finder1 = new AStarPathfinder(graph,
                                                         weightFunction,
                                                         hf);
        
        AbstractPathfinder finder2 = new DijkstraPathfinder(graph,
                                                            weightFunction);
        
        long start = System.currentTimeMillis();
        List<Integer> path1 = finder1.search(sourceNodeId, targetNodeId);
        long end = System.currentTimeMillis();
        
        System.out.println("A* in " + (end - start) + " milliseconds.");
        
        path1.forEach(System.out::println);
        System.out.println();
        
        start = System.currentTimeMillis();
        List<Integer> path2 = finder2.search(sourceNodeId, targetNodeId);
        end = System.currentTimeMillis();
        
        System.out.println("Dijkstra in " + (end - start) + " milliseconds.");
        
        path2.forEach(System.out::println);
        
        System.out.println("Algorithms agree: " + path1.equals(path2));
    }
    
    private static DirectedGraph getRandomGraph(int nodes, 
                                                int arcs, 
                                                Random random) {
        DirectedGraph graph = new DirectedGraph();
        
        for (int id = 0; id < nodes; ++id) {
            graph.addNode(id);
        }
        
        List<Integer> graphNodeList = new ArrayList<>(graph.getNodeList());
        
        while (arcs-- > 0) {
            Integer tailNodeId = choose(graphNodeList, random);
            Integer headNodeId = choose(graphNodeList, random);
            graph.addArc(tailNodeId, headNodeId);
        }
        
        return graph;
    }
     
    private static DirectedGraphNodeCoordinates 
        getCoordinates(DirectedGraph graph, Random random) {
        DirectedGraphNodeCoordinates coordinates =
                new DirectedGraphNodeCoordinates();
        
        for (Integer nodeId : graph.getNodeList()) {
            coordinates.put(nodeId, randomPoint(1000.0, 1000.0, random));
        }
        
        return coordinates;
    }
    
    private static DirectedGraphWeightFunction 
        getWeightFunction(DirectedGraph graph,
                          DirectedGraphNodeCoordinates coordinates) {
        DirectedGraphWeightFunction weightFunction = 
                new DirectedGraphWeightFunction();
        
        for (Integer nodeId : graph.getNodeList()) {
            Point2D.Double p1 = coordinates.get(nodeId);
            
            for (Integer childNodeId : graph.getChildrenOf(nodeId)) {
                Point2D.Double p2 = coordinates.get(childNodeId);
                double distance = p1.distance(p2);
                weightFunction.put(nodeId, childNodeId, 1.2 * distance);
            }
        }
        
        return weightFunction;
    }
        
    private static Point2D.Double randomPoint(double width,
                                              double height,
                                              Random random) {
        return new Point2D.Double(width * random.nextDouble(),
                                  height * random.nextDouble());
    }
    
    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
}