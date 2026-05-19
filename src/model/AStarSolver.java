package model;

import java.util.*;

public class AStarSolver {
    public static double heuristic(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    public final Grid grid;
    public final Node start, goal;

    public final PriorityQueue<Node> openSet = new PriorityQueue<>();
    public final Set<Node> openSetLookup = new HashSet<>();
    public final Set<Node> closedSet = new HashSet<>();
    public boolean done = false, found = false;
    public int steps = 0;

    private final Deque<SolverState> history = new ArrayDeque<>();

    public AStarSolver(Grid grid, Node start, Node goal) {
        this.grid = grid; this.start = start; this.goal = goal;
        grid.reset();
        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.h;
        openSet.add(start);
        openSetLookup.add(start);
        saveState();
    }

    public boolean step() {
        if (done || openSet.isEmpty()) { done = true; return false; }
        saveState();
        Node current = openSet.poll();
        openSetLookup.remove(current);
        if (current.equals(goal)) { done = true; found = true; return false; }
        closedSet.add(current);
        steps++;
        for (Node nb : grid.neighbors(current)) {
            if (closedSet.contains(nb)) continue;
            double tentG = current.g + 1.0;
            if (tentG < nb.g || !openSetLookup.contains(nb)) {
                nb.g = tentG;
                nb.h = heuristic(nb, goal);
                nb.f = nb.g + nb.h;
                nb.parent = current;
                if (!openSetLookup.contains(nb)) {
                    openSet.add(nb);
                    openSetLookup.add(nb);
                }
            }
        }
        return true;
    }

    public boolean stepBack() {
        if (history.isEmpty()) return false;
        SolverState prev = history.removeLast();
        restoreState(prev);
        steps = Math.max(0, steps - 1);
        done = false;
        found = false;
        return true;
    }

    public boolean canStepBack() {
        return !history.isEmpty();
    }

    private void saveState() {
        List<Node> openList = new ArrayList<>(openSetLookup);
        List<Node> closedList = new ArrayList<>(closedSet);
        Map<Node, NodeSnapshot> nodeStates = new HashMap<>();
        for (Node n : openSetLookup) {
            nodeStates.put(n, new NodeSnapshot(n.g, n.h, n.f, n.parent));
        }
        for (Node n : closedSet) {
            nodeStates.put(n, new NodeSnapshot(n.g, n.h, n.f, n.parent));
        }
        history.add(new SolverState(openList, closedList, nodeStates));
    }

    private void restoreState(SolverState state) {
        openSet.clear();
        openSetLookup.clear();
        closedSet.clear();
        for (Node n : state.openList) {
            openSet.add(n);
            openSetLookup.add(n);
        }
        closedSet.addAll(state.closedList);
        for (Map.Entry<Node, NodeSnapshot> entry : state.nodeStates.entrySet()) {
            Node n = entry.getKey();
            NodeSnapshot snap = entry.getValue();
            n.g = snap.g;
            n.h = snap.h;
            n.f = snap.f;
            n.parent = snap.parent;
        }
    }

    private record NodeSnapshot(double g, double h, double f, Node parent) {}
    private record SolverState(List<Node> openList, List<Node> closedList,
                               Map<Node, NodeSnapshot> nodeStates) {}

    public List<Node> reconstructPath() {
        List<Node> path = new ArrayList<>();
        Node cur = goal;
        while (cur != null) { path.add(0, cur); cur = cur.parent; }
        return path.isEmpty() ? null : path;
    }
}
