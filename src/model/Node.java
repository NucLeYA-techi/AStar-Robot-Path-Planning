package model;

public class Node implements Comparable<Node> {
    public final int row, col;
    public double g, h, f;
    public Node parent;
    public boolean wall;

    public Node(int row, int col) {
        this.row = row; this.col = col;
    }

    public void reset() { g = h = f = 0; parent = null; }

    @Override public int compareTo(Node o) { return Double.compare(f, o.f); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Node)) return false;
        Node n = (Node) o; return row == n.row && col == n.col;
    }

    @Override public int hashCode() { return row * 1000 + col; }
}
