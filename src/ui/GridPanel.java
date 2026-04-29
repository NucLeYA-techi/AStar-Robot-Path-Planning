package ui;

import model.*;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GridPanel extends JPanel {
    public static final int GAP = 1;
    public int CELL = 28;

    public enum EditMode { WALL, START, GOAL, ERASE }

    public Grid grid;
    public Theme theme;
    public EditMode editMode = EditMode.WALL;
    public Node startNode, goalNode;

    public Set<Node> openSet = new HashSet<>();
    public Set<Node> closedSet = new HashSet<>();
    public List<Node> path = new ArrayList<>();
    public Node robotCurrentPosition;

    public Map<Node, Float> openFade = new HashMap<>();
    public Map<Node, Float> closedFade = new HashMap<>();

    public Runnable onChange;

    public GridPanel(Grid grid, Theme theme) {
        this.grid = grid;
        this.theme = theme;

        int maxDim = Math.max(grid.rows, grid.cols);
        if (maxDim <= 20) CELL = 28;
        else if (maxDim <= 30) CELL = 20;
        else if (maxDim <= 40) CELL = 16;
        else CELL = 12;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            boolean dragging = false;
            boolean drawingWall = true;

            @Override public void mousePressed(MouseEvent e) {
                dragging = true;
                Node n = nodeAt(e.getX(), e.getY());
                if (n == null) return;
                if (editMode == EditMode.WALL) {
                    drawingWall = !n.wall;
                    n.wall = drawingWall;
                    if (n.equals(startNode)) startNode = null;
                    if (n.equals(goalNode)) goalNode = null;
                } else if (editMode == EditMode.ERASE) {
                    n.wall = false;
                } else if (editMode == EditMode.START) {
                    if (!n.wall) { startNode = n; editMode = EditMode.WALL; }
                } else if (editMode == EditMode.GOAL) {
                    if (!n.wall) { goalNode = n; editMode = EditMode.WALL; }
                }
                clearVisualization();
                repaint();
                if (onChange != null) onChange.run();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (!dragging) return;
                Node n = nodeAt(e.getX(), e.getY());
                if (n == null) return;
                if (editMode == EditMode.WALL) {
                    n.wall = drawingWall;
                    if (n.equals(startNode)) startNode = null;
                    if (n.equals(goalNode)) goalNode = null;
                } else if (editMode == EditMode.ERASE) {
                    n.wall = false;
                }
                clearVisualization();
                repaint();
                if (onChange != null) onChange.run();
            }

            @Override public void mouseReleased(MouseEvent e) { dragging = false; }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public Node nodeAt(int px, int py) {
        int step = CELL + GAP;
        int c = px / step, r = py / step;
        return grid.get(r, c);
    }

    public void clearVisualization() {
        openSet.clear(); closedSet.clear(); path.clear();
        openFade.clear(); closedFade.clear();
        robotCurrentPosition = null;
    }

    public void applyTheme(Theme t) { this.theme = t; repaint(); }

    @Override public Dimension getPreferredSize() {
        int step = CELL + GAP;
        return new Dimension(grid.cols * step, grid.rows * step);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int step = CELL + GAP;
        for (int r = 0; r < grid.rows; r++) {
            for (int c = 0; c < grid.cols; c++) {
                Node n = grid.nodes[r][c];
                int x = c * step, y = r * step;
                Color base = cellColor(n);
                g2.setColor(base);
                g2.fillRoundRect(x, y, CELL, CELL, 6, 6);
                if (path.contains(n) && !n.equals(startNode) && !n.equals(goalNode)) {
                    g2.setColor(new Color(theme.cellPath.getRed(), theme.cellPath.getGreen(),
                            theme.cellPath.getBlue(), 60));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(x - 1, y - 1, CELL + 2, CELL + 2, 8, 8);
                    g2.setStroke(new BasicStroke(1f));
                }
                if (theme.name.equals("Light")) {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(0.6f));
                    g2.drawRoundRect(x, y, CELL, CELL, 6, 6);
                    g2.setStroke(new BasicStroke(1f));
                }
                if (n.equals(robotCurrentPosition) && !n.equals(startNode) && !n.equals(goalNode))
                    drawIcon(g2, x, y, "R", new Color(255, 255, 255));
                else if (n.equals(startNode)) drawIcon(g2, x, y, "S", theme.cellStart);
                else if (n.equals(goalNode)) drawIcon(g2, x, y, "G", theme.cellGoal);
            }
        }
    }

    public Color cellColor(Node n) {
        if (n.wall) return theme.cellWall;
        if (n.equals(robotCurrentPosition) && !n.equals(startNode) && !n.equals(goalNode))
            return new Color(255, 140, 0);
        if (n.equals(startNode)) return theme.cellStart;
        if (n.equals(goalNode)) return theme.cellGoal;
        if (path.contains(n)) return theme.cellPath;
        if (closedSet.contains(n)) {
            float a = closedFade.getOrDefault(n, 1f);
            return blend(theme.cellClosed, theme.cellEmpty, a);
        }
        if (openSet.contains(n)) {
            float a = openFade.getOrDefault(n, 1f);
            return blend(theme.cellOpen, theme.cellEmpty, a);
        }
        return theme.cellEmpty;
    }

    public Color blend(Color a, Color b, float t) {
        float s = 1 - t;
        return new Color(
                (int)(a.getRed() * t + b.getRed() * s),
                (int)(a.getGreen() * t + b.getGreen() * s),
                (int)(a.getBlue() * t + b.getBlue() * s));
    }

    public void drawIcon(Graphics2D g2, int x, int y, String label, Color base) {
        g2.setColor(base.darker());
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (CELL - fm.stringWidth(label)) / 2;
        int ty = y + (CELL + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(label, tx, ty);
    }
}
