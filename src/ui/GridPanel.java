package ui;

import model.*;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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

    private Node hoveredNode = null;

    /**
     * Interaction state for the guided first-use flow:
     *   NEED_START  → next click places start
     *   NEED_GOAL   → next click places goal
     *   FREE        → normal wall/erase editing
     */
    public enum SetupState { NEED_START, NEED_GOAL, FREE }
    public SetupState setupState = SetupState.NEED_START;

    public Runnable onChange;

    public GridPanel(Grid grid, Theme theme) {
        this.grid = grid;
        this.theme = theme;

        int maxDim = Math.max(grid.rows, grid.cols);
        if (maxDim <= 15)       CELL = 55;
        else if (maxDim <= 20)  CELL = 45;
        else if (maxDim <= 30)  CELL = 35;
        else if (maxDim <= 40)  CELL = 28;
        else if (maxDim <= 60)  CELL = 20;
        else                    CELL = 16;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            boolean dragging = false;
            boolean drawingWall = true;

            @Override public void mousePressed(MouseEvent e) {
                dragging = true;
                Node n = nodeAt(e.getX(), e.getY());
                if (n == null) return;

                // Guided setup flow takes priority over editMode
                if (setupState == SetupState.NEED_START) {
                    if (!n.wall) {
                        startNode = n;
                        setupState = SetupState.NEED_GOAL;
                        clearVisualization();
                        repaint();
                        if (onChange != null) onChange.run();
                    }
                    return;
                }
                if (setupState == SetupState.NEED_GOAL) {
                    if (!n.wall && !n.equals(startNode)) {
                        goalNode = n;
                        setupState = SetupState.FREE;
                        editMode = EditMode.WALL;
                        clearVisualization();
                        repaint();
                        if (onChange != null) onChange.run();
                    }
                    return;
                }

                // FREE mode — normal editing
                if (editMode == EditMode.WALL) {
                    if (n.equals(startNode) || n.equals(goalNode)) return;
                    drawingWall = !n.wall;
                    n.wall = drawingWall;
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
                if (!dragging || setupState != SetupState.FREE) return;
                Node n = nodeAt(e.getX(), e.getY());
                updateHover(n);
                if (n == null) return;
                if (editMode == EditMode.WALL) {
                    if (n.equals(startNode) || n.equals(goalNode)) return;
                    n.wall = drawingWall;
                } else if (editMode == EditMode.ERASE) {
                    n.wall = false;
                }
                clearVisualization();
                repaint();
                if (onChange != null) onChange.run();
            }

            @Override public void mouseMoved(MouseEvent e)  { updateHover(nodeAt(e.getX(), e.getY())); }
            @Override public void mouseReleased(MouseEvent e) { dragging = false; }
            @Override public void mouseExited(MouseEvent e)  { updateHover(null); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /** Call after a scenario loads start/goal externally to skip the guided flow. */
    public void markSetupComplete() {
        if (startNode != null && goalNode != null) setupState = SetupState.FREE;
        else if (startNode != null)                setupState = SetupState.NEED_GOAL;
        else                                       setupState = SetupState.NEED_START;
    }

    /** Full reset — guided flow restarts from scratch. */
    public void resetSetup() {
        startNode = null;
        goalNode  = null;
        setupState = SetupState.NEED_START;
    }

    private void updateHover(Node n) {
        if (hoveredNode != n) { hoveredNode = n; repaint(); }
    }

    public Node nodeAt(int px, int py) {
        int step = CELL + GAP;
        return grid.get(py / step, px / step);
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

    public BufferedImage toImage() {
        int step = CELL + GAP;
        BufferedImage img = new BufferedImage(
                grid.cols * step, grid.rows * step, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(theme.bg);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        paintGrid(g2);
        g2.dispose();
        return img;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintGrid(g2);
    }

    private void paintGrid(Graphics2D g2) {
        int step = CELL + GAP;
        for (int r = 0; r < grid.rows; r++) {
            for (int c = 0; c < grid.cols; c++) {
                Node n = grid.nodes[r][c];
                int x = c * step, y = r * step;

                // Fill cell
                g2.setColor(cellColor(n));
                g2.fillRoundRect(x, y, CELL, CELL, 6, 6);

                // Hover highlight
                if (n.equals(hoveredNode) && !n.wall
                        && !n.equals(startNode) && !n.equals(goalNode)
                        && !n.equals(robotCurrentPosition)) {
                    g2.setColor(new Color(theme.accent.getRed(),
                            theme.accent.getGreen(), theme.accent.getBlue(), 130));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(x + 1, y + 1, CELL - 2, CELL - 2, 6, 6);
                    g2.setStroke(new BasicStroke(1f));
                }

                // Path glow border — skip robot cell for clean separation
                if (path.contains(n) && !n.equals(startNode) && !n.equals(goalNode)
                        && !n.equals(robotCurrentPosition)) {
                    g2.setColor(new Color(theme.cellPath.getRed(),
                            theme.cellPath.getGreen(), theme.cellPath.getBlue(), 70));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(x - 1, y - 1, CELL + 2, CELL + 2, 8, 8);
                    g2.setStroke(new BasicStroke(1f));
                }

                // Light-theme cell border
                if (theme.name.equals("Light")) {
                    g2.setColor(new Color(0, 0, 0, 25));
                    g2.setStroke(new BasicStroke(0.6f));
                    g2.drawRoundRect(x, y, CELL, CELL, 6, 6);
                    g2.setStroke(new BasicStroke(1f));
                }

                // A* value labels (f centered, g top-left, h top-right)
                drawNodeValues(g2, x, y, n);

                // Icons — robot emoji takes priority over start/goal during animation
                if (n.equals(robotCurrentPosition) && !n.equals(startNode)) {
                    drawRobotEmoji(g2, x, y);
                } else if (n.equals(startNode)) {
                    drawNodeLabel(g2, x, y, "S", Color.WHITE);
                } else if (n.equals(goalNode)) {
                    drawNodeLabel(g2, x, y, "G", Color.WHITE);
                }
            }
        }
    }

    public Color cellColor(Node n) {
        if (n.wall) return theme.cellWall;
        if (n.equals(robotCurrentPosition) && !n.equals(startNode))
            return theme.cellRobot;
        if (n.equals(startNode)) return theme.cellStart;
        if (n.equals(goalNode))  return theme.cellGoal;
        if (path.contains(n))   return theme.cellPath;
        if (closedSet.contains(n)) return blend(theme.cellClosed, theme.cellEmpty, closedFade.getOrDefault(n, 1f));
        if (openSet.contains(n))   return blend(theme.cellOpen,   theme.cellEmpty, openFade.getOrDefault(n, 1f));
        return theme.cellEmpty;
    }

    public Color blend(Color a, Color b, float t) {
        float s = 1 - t;
        return new Color(
                (int)(a.getRed()   * t + b.getRed()   * s),
                (int)(a.getGreen() * t + b.getGreen() * s),
                (int)(a.getBlue()  * t + b.getBlue()  * s));
    }

    /** Draws "S" or "G" with a dark outline for visibility on any background. */
    private void drawNodeLabel(Graphics2D g2, int x, int y, String label, Color fg) {
        int fontSize = Math.max(12, CELL / 2);
        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (CELL - fm.stringWidth(label)) / 2;
        int ty = y + (CELL + fm.getAscent() - fm.getDescent()) / 2;
        // Dark outline for contrast
        g2.setColor(new Color(0, 0, 0, 160));
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0) g2.drawString(label, tx + dx, ty + dy);
        g2.setColor(fg);
        g2.drawString(label, tx, ty);
    }

    /** Draws f(x) centered, g(x) top-left, h(x) top-right for visited nodes. */
    private void drawNodeValues(Graphics2D g2, int x, int y, Node n) {
        if (n.wall) return;
        boolean visited = openSet.contains(n) || closedSet.contains(n)
                || n.equals(startNode) || n.equals(goalNode);
        if (!visited) return;
        if (n.g == 0 && n.h == 0 && !n.equals(startNode)) return;

        boolean isStartOrGoal = n.equals(startNode) || n.equals(goalNode);

        String gStr = String.valueOf((int) n.g);
        String hStr = String.valueOf((int) n.h);
        String fStr = String.valueOf((int) n.f);

        // Small font for corner values — sized to never overlap
        int smFont = Math.max(8, Math.min(10, CELL / 4));
        g2.setFont(new Font("Monospaced", Font.PLAIN, smFont));
        FontMetrics sm = g2.getFontMetrics();
        int smH = sm.getAscent();

        // Top-left: g(x) — cyan
        drawTextWithOutline(g2, gStr, x + 3, y + smH + 1,
                new Color(100, 215, 255), 180);

        // Top-right: h(x) — amber
        int hX = x + CELL - sm.stringWidth(hStr) - 3;
        drawTextWithOutline(g2, hStr, hX, y + smH + 1,
                new Color(255, 210, 100), 180);

        // Center: f(x) — skip on start/goal (S/G label is the identifier)
        if (!isStartOrGoal) {
            int lgFont = Math.max(9, Math.min(13, CELL / 3));
            g2.setFont(new Font("Monospaced", Font.BOLD, lgFont));
            FontMetrics lg = g2.getFontMetrics();
            int fX = x + (CELL - lg.stringWidth(fStr)) / 2;
            int fY = y + (CELL + lg.getAscent() - lg.getDescent()) / 2;
            drawTextWithOutline(g2, fStr, fX, fY, Color.WHITE, 200);
        }
    }

    /** Draws text with a dark outline for visibility on any background. */
    private void drawTextWithOutline(Graphics2D g2, String text, int x, int y,
                                     Color color, int alpha) {
        g2.setColor(new Color(0, 0, 0, alpha));
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0) g2.drawString(text, x + dx, y + dy);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }

    /** Draws the robot emoji centered in the cell with a contrasting glow ring. */
    private void drawRobotEmoji(Graphics2D g2, int x, int y) {
        // Outer glow ring — bright white halo
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, CELL, CELL, 8, 8);

        // Inner glow ring — accent color
        g2.setColor(new Color(theme.accent.getRed(), theme.accent.getGreen(),
                theme.accent.getBlue(), 120));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 6, 6);
        g2.setStroke(new BasicStroke(1f));

        // Emoji rendering — only pick fonts actually installed on this system
        int fontSize = Math.max(12, CELL - 4);
        String emoji = "\uD83E\uDD16"; // 🤖

        Set<String> installedFonts = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

        String[] preferred = {
            "Noto Color Emoji", "Segoe UI Emoji", "Apple Color Emoji",
            "Twitter Color Emoji", "EmojiOne", "Symbola"
        };

        Font chosen = null;
        for (String name : preferred) {
            if (installedFonts.contains(name)) {
                Font f = new Font(name, Font.PLAIN, fontSize);
                if (f.canDisplayUpTo(emoji) == -1) { chosen = f; break; }
            }
        }

        if (chosen != null) {
            g2.setFont(chosen);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (CELL - fm.stringWidth(emoji)) / 2;
            int ty = y + (CELL + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawString(emoji, tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(emoji, tx, ty);
        } else {
            // Fallback — draw a robot icon using Graphics2D primitives
            drawRobotIcon(g2, x, y);
        }
    }

    /** Draws a stylized robot icon using Graphics2D when emoji fonts are unavailable. */
    private void drawRobotIcon(Graphics2D g2, int x, int y) {
        int cx = x + CELL / 2;
        int cy = y + CELL / 2;
        int s = Math.max(6, CELL / 3);  // base unit

        // Antenna
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cx, cy - s - 2, cx, cy - s + 2);
        // Antenna tip — small circle
        g2.fillOval(cx - 2, cy - s - 4, 4, 4);

        // Head — rounded rectangle
        int headW = s * 2;
        int headH = (int)(s * 1.4);
        int headX = cx - headW / 2;
        int headY = cy - headH / 2 - 1;
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(headX, headY, headW, headH, 4, 4);

        // Eyes — two small dots
        g2.setColor(theme.cellRobot.darker());
        int eyeY = headY + headH / 3;
        g2.fillOval(cx - s / 2 - 1, eyeY, 3, 3);
        g2.fillOval(cx + s / 2 - 2, eyeY, 3, 3);

        // Mouth — small line
        g2.drawLine(cx - s / 3, headY + headH * 2 / 3, cx + s / 3, headY + headH * 2 / 3);

        // Body — rectangle below head
        int bodyY = headY + headH + 1;
        int bodyH = (int)(s * 1.2);
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRoundRect(cx - s / 2 - 1, bodyY, s + 2, bodyH, 3, 3);

        g2.setStroke(new BasicStroke(1f));
    }
}
