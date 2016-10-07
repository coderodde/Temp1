package net.coderodde.graph.pathfinding.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.HeuristicFunction;

/**
 * This pathfinding algorithm is due to Wim Pijls and Henk Post in "Yet another
 * bidirectional algorithm for shortest paths." 15 June 2009.
 * <p>
 * <b>This class is not thread-safe.</b> If you need it in different threads,
 * make sure each thread has its own object of this class.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 6, 2016)
 */
public final class NBAStarPathfinder extends AbstractPathfinder {

    private final HeuristicFunction heuristicFunction;
    private final PriorityQueue<HeapEntry> OPENA = new PriorityQueue<>();
    private final PriorityQueue<HeapEntry> OPENB = new PriorityQueue<>();
    private final Map<Integer, Integer> PARENTSA = new HashMap<>();
    private final Map<Integer, Integer> PARENTSB = new HashMap<>();
    private final Map<Integer, Double> DISTANCEA = new HashMap<>();
    private final Map<Integer, Double> DISTANCEB = new HashMap<>();
    private final Set<Integer> CLOSED = new HashSet<>();

    private double fA;
    private double fB;
    private double bestPathLength;
    private Integer touchNode;
    private Integer sourceNodeId;
    private Integer targetNodeId;

    public NBAStarPathfinder(DirectedGraph graph,
            DirectedGraphWeightFunction weightFunction,
            HeuristicFunction heuristicFunction) {
        super(graph, weightFunction);
        this.heuristicFunction
                = Objects.requireNonNull(heuristicFunction,
                        "The input heuristic function is null.");
    }

    @Override
    public List<Integer> search(int sourceNodeId, int targetNodeId) {
        if (sourceNodeId == targetNodeId) {
            return new ArrayList<>(Arrays.asList(sourceNodeId));
        }

        init(sourceNodeId, targetNodeId);

        while (!OPENA.isEmpty() && !OPENB.isEmpty()) {
            if (OPENA.size() < OPENB.size()) {
                expandInForwardDirection();
            } else {
                expandInBackwardDirection();
            }
        }

        if (touchNode == null) {
            return new ArrayList<>();
        }

        return tracebackPath(touchNode, PARENTSA, PARENTSB);
    }

    private void expandInForwardDirection() {
        Integer currentNode = OPENA.remove().getNode();

//        if (CLOSED.contains(currentNode)) {
//            return;
//        }

        CLOSED.add(currentNode);

        if (DISTANCEA.get(currentNode) +
                heuristicFunction.estimateDistanceBetween(currentNode,
                                                          targetNodeId)
                >= bestPathLength
                ||
                DISTANCEA.get(currentNode) +
                fB - 
                heuristicFunction.estimateDistanceBetween(currentNode,
                                                          sourceNodeId)
                >= bestPathLength) {
            // Reject the 'currentNode'.
        } else {
            // Stabilize the 'currentNode'.
            for (Integer childNode : graph.getChildrenOf(currentNode)) {
                if (CLOSED.contains(childNode)) {
                    continue;
                }

                double tentativeDistance
                        = DISTANCEA.get(currentNode)
                        + weightFunction.get(currentNode, childNode);

                if (!DISTANCEA.containsKey(childNode)
                        || 
                        DISTANCEA.get(childNode) > tentativeDistance) {
                    DISTANCEA.put(childNode, tentativeDistance);
                    PARENTSA.put(childNode, currentNode);
                    HeapEntry e
                            = new HeapEntry(
                                    childNode,
                                    tentativeDistance
                                    + heuristicFunction
                                    .estimateDistanceBetween(childNode,
                                                             targetNodeId));
                    OPENA.add(e);

                    if (DISTANCEB.containsKey(childNode)) {
                        double pathLength = tentativeDistance
                                + DISTANCEB.get(childNode);

                        if (bestPathLength > pathLength) {
                            bestPathLength = pathLength;
                            touchNode = childNode;
                        }
                    }
                }
            }
        }

        if (!OPENA.isEmpty()) {
            fA = OPENA.peek().getDistance();
//            Integer node = OPENA.peek().getNode();
//            fA = DISTANCEA.get(node)
//                    + heuristicFunction.estimateDistanceBetween(node, 
//                                                                targetNodeId);
        }
    }

    private void expandInBackwardDirection() {
        Integer currentNode = OPENB.remove().getNode();

//        if (CLOSED.contains(currentNode)) {
//            return;
//        }

        CLOSED.add(currentNode);

        if (DISTANCEB.get(currentNode) +
                heuristicFunction
                .estimateDistanceBetween(currentNode,
                                         sourceNodeId)
                >= bestPathLength
                || 
                DISTANCEB.get(currentNode) +
                fA -
                heuristicFunction
                .estimateDistanceBetween(currentNode, targetNodeId)
                >= bestPathLength) {
            // Reject the node 'currentNode'.
        } else {
            for (Integer parentNode : graph.getParentsOf(currentNode)) {
                if (CLOSED.contains(parentNode)) {
                    continue;
                }

                double tentativeDistance
                        = DISTANCEB.get(currentNode)
                        + weightFunction.get(parentNode, currentNode);

                if (!DISTANCEB.containsKey(parentNode)
                        || DISTANCEB.get(parentNode)
                        > tentativeDistance) {
                    DISTANCEB.put(parentNode, tentativeDistance);
                    PARENTSB.put(parentNode, currentNode);
                    HeapEntry e
                            = new HeapEntry(parentNode,
                                    tentativeDistance
                                    + heuristicFunction
                                    .estimateDistanceBetween(parentNode,
                                                             targetNodeId));
                    OPENB.add(e);
                    
                    if (DISTANCEA.containsKey(parentNode)) {
                        double pathLength = tentativeDistance
                                + DISTANCEA.get(parentNode);

                        if (bestPathLength > pathLength) {
                            bestPathLength = pathLength;
                            touchNode = parentNode;
                        }
                    }
                }
            }
        }

        if (!OPENB.isEmpty()) {
//            Integer node = OPENB.peek().getNode();
            fB = OPENB.peek().getDistance();
            
//            fB = DISTANCEB.get(node)
//                    + heuristicFunction
//                    .estimateDistanceBetween(node, sourceNodeId);
//            System.out.println("hfds: " + OPENB.peek().getDistance() + ":" + fB);
        }
    }

    private void init(Integer sourceNodeId, Integer targetNodeId) {
        OPENA.clear();
        OPENB.clear();
        PARENTSA.clear();
        PARENTSB.clear();
        DISTANCEA.clear();
        DISTANCEB.clear();
        CLOSED.clear();

        double totalDistance
                = heuristicFunction.estimateDistanceBetween(sourceNodeId,
                        targetNodeId);

        fA = totalDistance;
        fB = totalDistance;
        bestPathLength = Double.MAX_VALUE;
        touchNode = null;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;

        OPENA.add(new HeapEntry(sourceNodeId, fA));
        OPENB.add(new HeapEntry(targetNodeId, fB));
        PARENTSA.put(sourceNodeId, null);
        PARENTSB.put(targetNodeId, null);
        DISTANCEA.put(sourceNodeId, 0.0);
        DISTANCEB.put(targetNodeId, 0.0);
    }
}
