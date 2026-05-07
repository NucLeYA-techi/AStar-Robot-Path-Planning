package util;

import model.Grid;
import model.Node;

import java.util.*;

/**
 * Generates visually distinct, structurally intentional obstacle layouts.
 * Each scenario accepts a seed so repeated calls produce varied-but-themed layouts.
 * Every layout is BFS-validated; an L-shaped corridor is carved if needed.
 */
public class ScenarioManager {

    public record ScenarioResult(int startRow, int startCol, int goalRow, int goalCol) {}

    public enum Scenario {
        WAREHOUSE("Warehouse"),
        OFFICE("Office Layout"),
        CITY_GRID("City Grid"),
        DENSE_MAZE("Dense Maze"),
        SPARSE("Sparse Obstacles");

        public final String label;
        Scenario(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    /** Apply with a fixed seed (used by "Apply Scenario" button — deterministic first load). */
    public static ScenarioResult apply(Scenario scenario, Grid grid) {
        return applyWithSeed(scenario, grid, 42L);
    }

    /** Apply with a given seed (used by "Generate Obstacles" for varied-but-themed layouts). */
    public static ScenarioResult applyWithSeed(Scenario scenario, Grid grid, long seed) {
        clearAll(grid);
        Random rng = new Random(seed);
        ScenarioResult result = switch (scenario) {
            case WAREHOUSE  -> generateWarehouse(grid, rng);
            case OFFICE     -> generateOffice(grid, rng);
            case CITY_GRID  -> generateTraffic(grid, rng);
            case DENSE_MAZE -> generateMaze(grid, rng);
            case SPARSE     -> generateSparse(grid, rng);
        };
        ensurePath(grid, result.startRow(), result.startCol(),
                         result.goalRow(),  result.goalCol());
        return result;
    }

    // ── 1. WAREHOUSE ─────────────────────────────────────────────────────
    // Horizontal shelf rows spanning most of the width.
    // Aisle positions and shelf lengths vary per seed.
    private static ScenarioResult generateWarehouse(Grid grid, Random rng) {
        int R = grid.rows, C = grid.cols;

        // Shelf rows spaced 3 apart; spacing offset varies by 0–1
        int offset = rng.nextInt(2);
        int shelfGap = 3;

        for (int r = shelfGap - offset; r < R - 1; r += shelfGap) {
            // Each shelf row has 2–3 aisle gaps at random column positions
            Set<Integer> aisles = new HashSet<>();
            int numAisles = 2 + rng.nextInt(2);
            while (aisles.size() < numAisles)
                aisles.add(1 + rng.nextInt(C - 2));

            for (int c = 1; c < C - 1; c++) {
                if (!aisles.contains(c)) wall(grid, r, c);
            }
        }
        return new ScenarioResult(0, 0, R - 1, C - 1);
    }

    // ── 2. OFFICE LAYOUT ─────────────────────────────────────────────────
    // Rooms divided by spine walls with doorways; inner sub-walls vary per seed.
    private static ScenarioResult generateOffice(Grid grid, Random rng) {
        int R = grid.rows, C = grid.cols;

        // Spine positions vary slightly around the midpoint
        int midR = R / 2 + rng.nextInt(3) - 1;
        int midC = C / 2 + rng.nextInt(3) - 1;
        midR = Math.max(2, Math.min(R - 3, midR));
        midC = Math.max(2, Math.min(C - 3, midC));

        // Horizontal spine — door at random column
        int doorC = 1 + rng.nextInt(C - 2);
        for (int c = 0; c < C; c++)
            if (c != doorC) wall(grid, midR, c);

        // Vertical spine — door at random row
        int doorR = 1 + rng.nextInt(R - 2);
        for (int r = 0; r < R; r++)
            if (r != doorR) wall(grid, r, midC);

        // Inner wall in top-left quadrant (horizontal)
        if (midR > 3) {
            int innerR = 1 + rng.nextInt(midR - 2);
            int innerDoor = rng.nextInt(midC);
            for (int c = 0; c < midC; c++)
                if (c != innerDoor) wall(grid, innerR, c);
        }

        // Inner wall in bottom-right quadrant (vertical)
        if (C - midC > 3) {
            int innerC = midC + 1 + rng.nextInt(C - midC - 2);
            int innerDoor2 = midR + 1 + rng.nextInt(R - midR - 1);
            for (int r = midR + 1; r < R; r++)
                if (r != innerDoor2) wall(grid, r, innerC);
        }

        return new ScenarioResult(1, 1, R - 2, C - 2);
    }

    // ── 3. CITY / TRAFFIC GRID ───────────────────────────────────────────
    // Solid city blocks separated by roads. Block and road sizes vary per seed.
    private static ScenarioResult generateTraffic(Grid grid, Random rng) {
        int R = grid.rows, C = grid.cols;

        // Block dimensions vary: height 2–4, width 3–5
        int blockH = 2 + rng.nextInt(3);
        int blockW = 3 + rng.nextInt(3);
        int roadH  = 1 + rng.nextInt(2); // road height 1–2
        int roadW  = 1 + rng.nextInt(2); // road width 1–2
        int periodR = blockH + roadH;
        int periodC = blockW + roadW;

        // Row/col phase offset so roads don't always start at 0
        int phaseR = rng.nextInt(periodR);
        int phaseC = rng.nextInt(periodC);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                boolean onRoadRow = ((r + phaseR) % periodR) < roadH;
                boolean onRoadCol = ((c + phaseC) % periodC) < roadW;
                if (!onRoadRow && !onRoadCol) wall(grid, r, c);
            }
        }
        // Ensure corners are open for start/goal
        safe(grid, 0, 0, false);
        safe(grid, R - 1, C - 1, false);
        return new ScenarioResult(0, 0, R - 1, C - 1);
    }

    // ── 4. DENSE MAZE ────────────────────────────────────────────────────
    // Recursive-division maze — seed controls all passage positions.
    private static ScenarioResult generateMaze(Grid grid, Random rng) {
        divide(grid, 0, 0, grid.rows, grid.cols, rng);
        return new ScenarioResult(0, 0, grid.rows - 1, grid.cols - 1);
    }

    private static void divide(Grid grid, int r0, int c0, int height, int width, Random rng) {
        if (height < 3 || width < 3) return;

        boolean horizontal = (height > width) || (height == width && rng.nextBoolean());

        if (horizontal) {
            int wallRow = r0 + 1 + 2 * rng.nextInt(Math.max(1, (height - 1) / 2));
            int passCol = c0 + rng.nextInt(width);
            for (int c = c0; c < c0 + width; c++)
                if (c != passCol) wall(grid, wallRow, c);
            divide(grid, r0,          c0, wallRow - r0,              width, rng);
            divide(grid, wallRow + 1, c0, r0 + height - wallRow - 1, width, rng);
        } else {
            int wallCol = c0 + 1 + 2 * rng.nextInt(Math.max(1, (width - 1) / 2));
            int passRow = r0 + rng.nextInt(height);
            for (int r = r0; r < r0 + height; r++)
                if (r != passRow) wall(grid, r, wallCol);
            divide(grid, r0, c0,          height, wallCol - c0,              rng);
            divide(grid, r0, wallCol + 1, height, c0 + width - wallCol - 1,  rng);
        }
    }

    // ── 5. SPARSE OBSTACLES ──────────────────────────────────────────────
    // Isolated single-cell obstacles, ~12–18% density, no two adjacent.
    private static ScenarioResult generateSparse(Grid grid, Random rng) {
        int R = grid.rows, C = grid.cols;
        boolean[][] placed = new boolean[R][C];

        // Density varies 12–18% per seed
        double density = 0.12 + rng.nextDouble() * 0.06;
        int target = (int)(R * C * density);

        List<int[]> candidates = new ArrayList<>();
        for (int r = 1; r < R - 1; r++)
            for (int c = 1; c < C - 1; c++)
                candidates.add(new int[]{r, c});
        Collections.shuffle(candidates, rng);

        int count = 0;
        for (int[] cell : candidates) {
            if (count >= target) break;
            int r = cell[0], c = cell[1];
            if (hasNeighborWall(placed, r, c, R, C)) continue;
            placed[r][c] = true;
            wall(grid, r, c);
            count++;
        }
        return new ScenarioResult(0, 0, R - 1, C - 1);
    }

    private static boolean hasNeighborWall(boolean[][] placed, int r, int c, int R, int C) {
        for (int[] d : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < R && nc >= 0 && nc < C && placed[nr][nc]) return true;
        }
        return false;
    }

    // ── PATH VALIDATION ───────────────────────────────────────────────────
    private static void ensurePath(Grid grid, int sr, int sc, int gr, int gc) {
        if (bfsReachable(grid, sr, sc, gr, gc)) return;
        int r = sr, c = sc;
        while (r != gr) { safe(grid, r, c, false); r += (gr > r) ? 1 : -1; }
        while (c != gc) { safe(grid, r, c, false); c += (gc > c) ? 1 : -1; }
        safe(grid, gr, gc, false);
    }

    private static boolean bfsReachable(Grid grid, int sr, int sc, int gr, int gc) {
        Node src = grid.get(sr, sc), dst = grid.get(gr, gc);
        if (src == null || dst == null || src.wall || dst.wall) return false;
        Set<Node> visited = new HashSet<>();
        Queue<Node> q = new ArrayDeque<>();
        q.add(src); visited.add(src);
        while (!q.isEmpty()) {
            Node cur = q.poll();
            if (cur == dst) return true;
            for (Node nb : grid.neighbors(cur))
                if (visited.add(nb)) q.add(nb);
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static void clearAll(Grid grid) {
        for (Node[] row : grid.nodes) for (Node n : row) n.wall = false;
    }
    private static void wall(Grid grid, int r, int c) {
        Node n = grid.get(r, c); if (n != null) n.wall = true;
    }
    private static void safe(Grid grid, int r, int c, boolean state) {
        Node n = grid.get(r, c); if (n != null) n.wall = state;
    }
}
