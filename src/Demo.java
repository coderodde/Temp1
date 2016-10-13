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
import net.coderodde.graph.pathfinding.support.NBAStarPathfinder;

public class Demo {

    private static final int NODES = 100_000;
    private static final int ARCS = 500_000;
    private static final double PLANE_WIDTH = 1000.0;
    private static final double PLANE_HEIGHT = 1000.0;
    
    public static void main(String[] args) {
        long seed = 199942050968787L; System.nanoTime();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);

        long start = System.currentTimeMillis();
        DirectedGraph graph = getRandomGraph(NODES, ARCS, random);
        DirectedGraphNodeCoordinates coordinates = getCoordinates(graph, 
                                                                  random);
        DirectedGraphWeightFunction weightFunction = 
                getWeightFunction(graph, coordinates);

        Integer sourceNodeId = getSource(graph, coordinates);
        Integer targetNodeId = getTarget(graph, coordinates);
        
        long end = System.currentTimeMillis();

        System.out.println("Created the graph data structures in " +
                           (end - start) + " milliseconds.");

        System.out.println("Source: " + sourceNodeId);
        System.out.println("Target: " + targetNodeId);

        System.out.println();

        HeuristicFunction hf = new EuclideanHeuristicFunction(coordinates);

        AbstractPathfinder finder1 = new AStarPathfinder(graph,
                                                         weightFunction,
                                                         hf);

        AbstractPathfinder finder2 = new DijkstraPathfinder(graph,
                                                            weightFunction);

        AbstractPathfinder finder3 = new NBAStarPathfinder(graph, 
                                                           weightFunction,
                                                           hf);
        List<Integer> path1 = benchmark(finder1, sourceNodeId, targetNodeId);
        List<Integer> path2 = benchmark(finder2, sourceNodeId, targetNodeId);
        List<Integer> path3 = benchmark(finder3, sourceNodeId, targetNodeId);

        System.out.println("Algorithms agree: " +
                (path1.equals(path2) && path1.equals(path3)));
        System.out.println(getPathCost(path1, weightFunction));
        System.out.println(getPathCost(path3, weightFunction));
    }

    private static List<Integer> benchmark(AbstractPathfinder pathfinder,
                                           int sourceNode, 
                                           int targetNode) {
        long start = System.currentTimeMillis();
        List<Integer> path = pathfinder.search(sourceNode, targetNode);
        long end = System.currentTimeMillis();
        
        System.out.println(pathfinder.getClass().getSimpleName() + 
                           " in " + (end - start) + " milliseconds.");
        
        path.forEach(System.out::println);
        System.out.println();
        return path;
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
    
    private static Integer
         getClosestTo(DirectedGraph graph,
                      DirectedGraphNodeCoordinates coordinates,
                      Point2D.Double point) {
        double bestDistance = Double.POSITIVE_INFINITY;
        Integer bestNode = null;
        
        for (Integer node : graph.getNodeList()) {
            Point2D.Double nodePoint = coordinates.get(node);
            
            if (bestDistance > nodePoint.distance(point)) {
                bestDistance = nodePoint.distance(point);
                bestNode = node;
            }
        }
        
        return bestNode;
    }
         
    private static Integer getSource(DirectedGraph graph,
                                     DirectedGraphNodeCoordinates coordinates) {
        return getClosestTo(graph, coordinates, new Point2D.Double());
    }
    
    private static Integer getTarget(DirectedGraph graph,
                                     DirectedGraphNodeCoordinates coordinates) {
        return getClosestTo(graph, 
                            coordinates,
                            new Point2D.Double(PLANE_WIDTH, PLANE_HEIGHT));
    }
    
    private static double
         getPathCost(List<Integer> path,
                     DirectedGraphWeightFunction weightFunction) {
        double cost = 0.0;
        
        for (int i = 0; i < path.size() - 1; ++i) {
            cost += weightFunction.get(path.get(i), path.get(i + 1));
        }
        
        return cost;
    }
}
