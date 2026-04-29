package model;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    public final int rows, cols;
    public final Node[][] nodes;

    public Grid(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        nodes = new Node[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                nodes[r][c] = new Node(r, c);
    }

    public Node get(int r, int c) {
        return (r >= 0 && r < rows && c >= 0 && c < cols) ? nodes[r][c] : null;
    }

    public void reset() {
        for (Node[] row : nodes)
            for (Node n : row) n.reset();
    }

    public void clearPath() { reset(); }

    public List<Node> neighbors(Node n) {
        List<Node> list = new ArrayList<>();
        for (int[] d : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            Node nb = get(n.row + d[0], n.col + d[1]);
            if (nb != null && !nb.wall) list.add(nb);
        }
        return list;
    }
}
