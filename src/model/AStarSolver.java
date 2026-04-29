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

    public AStarSolver(Grid grid, Node start, Node goal) {
        this.grid = grid; this.start = start; this.goal = goal;
        grid.reset();
        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.h;
        openSet.add(start);
        openSetLookup.add(start);
    }

    public boolean step() {
        if (done || openSet.isEmpty()) { done = true; return false; }
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

    public List<Node> reconstructPath() {
        List<Node> path = new ArrayList<>();
        Node cur = goal;
        while (cur != null) { path.add(0, cur); cur = cur.parent; }
        return path.isEmpty() ? null : path;
    }
}
