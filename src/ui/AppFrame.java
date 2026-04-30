package ui;

import model.*;
import util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppFrame extends JFrame {
    public Theme theme;
    public Grid grid;
    public GridPanel gridPanel;
    public MetricsPanel metricsPanel;
    public LegendPanel legendPanel;

    public FlatButton btnSolve, btnReset, btnClear, btnTheme, btnRandom, btnChangeGrid;
    public FlatButton btnPause, btnResume, btnStep;
    public FlatButton btnModeWall, btnModeErase, btnModeStart, btnModeGoal;
    public JSlider speedSlider;
    public JLabel headerLabel, statusLabel, descLabel;

    public SwingWorker<Void, Void> worker;
    public AtomicBoolean running = new AtomicBoolean(false);
    public AtomicBoolean paused = new AtomicBoolean(false);
    public volatile AStarSolver activeSolver;

    public AppFrame(int rows, int cols) {
        super("Autonomous Robot Path Planning System");
        theme = Theme.dark();
        grid = new Grid(rows, cols);
        init();
    }

    public void init() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(700, 500));

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.bg);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildSidebar(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.panelBg);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(theme.border.darker());
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 18, 12, 18));

        headerLabel = new JLabel("Robot Navigation using A* Algorithm");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(theme.accent);

        descLabel = new JLabel("Simulates autonomous robot navigation using A* algorithm in an obstacle-filled environment.");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLabel.setForeground(theme.subText);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(headerLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(descLabel);

        statusLabel = new JLabel("Place Robot Start Position and Target Location.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(theme.subText);

        btnTheme = new FlatButton("☀ Light", theme);
        btnTheme.setPreferredSize(new Dimension(90, 30));
        btnTheme.addActionListener(e -> toggleTheme());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(titleBlock);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(statusLabel);
        right.add(btnTheme);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    public JPanel buildCenter() {
        gridPanel = new GridPanel(grid, theme);
        gridPanel.onChange = this::updateStatus;

        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(theme.bg);

        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.bg);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 14, 14, 8));
        wrap.add(scrollPane, BorderLayout.CENTER);
        return wrap;
    }

    public JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(theme.panelBg);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(theme.border);
                g.fillRect(0, 0, 1, getHeight());
            }
        };
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(14, 14, 14, 14));
        side.setMinimumSize(new Dimension(260, 0));

        side.add(sectionLabel("Edit Mode"));
        side.add(Box.createVerticalStrut(6));
        side.add(buildModeButtons());
        side.add(Box.createVerticalStrut(14));

        side.add(sectionLabel("Animation Speed"));
        side.add(Box.createVerticalStrut(6));
        speedSlider = new JSlider(1, 200, 40);
        speedSlider.setOpaque(false);
        speedSlider.setForeground(theme.text);
        side.add(speedSlider);
        side.add(Box.createVerticalStrut(14));

        side.add(sectionLabel("Actions"));
        side.add(Box.createVerticalStrut(6));
        btnSolve = new FlatButton("▶ Start Navigation", theme);
        btnPause = new FlatButton("⏸ Pause", theme);
        btnResume = new FlatButton("▶ Resume", theme);
        btnStep = new FlatButton("⏭ Step", theme);
        btnReset = new FlatButton("↺ Reset Navigation", theme);
        btnClear = new FlatButton("✕ Clear Environment", theme);
        btnRandom = new FlatButton("⚄ Generate Obstacles", theme);
        btnChangeGrid = new FlatButton("⊞ Change Grid Size", theme);
        for (FlatButton b : new FlatButton[]{btnSolve, btnPause, btnResume, btnStep, btnReset, btnClear, btnRandom, btnChangeGrid}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            b.setAlignmentX(LEFT_ALIGNMENT);
            side.add(b);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalStrut(8));

        side.add(sectionLabel("Metrics"));
        side.add(Box.createVerticalStrut(6));
        metricsPanel = new MetricsPanel(theme);
        metricsPanel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(metricsPanel);
        side.add(Box.createVerticalStrut(14));

        side.add(sectionLabel("Legend"));
        side.add(Box.createVerticalStrut(6));
        legendPanel = new LegendPanel(theme);
        legendPanel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(legendPanel);
        side.add(Box.createVerticalGlue());

        btnSolve.addActionListener(e -> startSolve());
        btnPause.addActionListener(e -> { paused.set(true); setStatus("Navigation paused."); });
        btnResume.addActionListener(e -> { paused.set(false); setStatus("Navigation resumed."); });
        btnStep.addActionListener(e -> doStep());
        btnReset.addActionListener(e -> resetPath());
        btnClear.addActionListener(e -> clearAll());
        btnRandom.addActionListener(e -> placeRandomMaze());
        btnChangeGrid.addActionListener(e -> changeGridSize());

        return side;
    }

    public JPanel buildModeButtons() {
        btnModeWall = modeBtn("✏ Obstacle");
        btnModeErase = modeBtn("⌫ Erase");
        btnModeStart = modeBtn("S Robot");
        btnModeGoal = modeBtn("G Target");

        btnModeWall.addActionListener(e -> setMode(GridPanel.EditMode.WALL));
        btnModeErase.addActionListener(e -> setMode(GridPanel.EditMode.ERASE));
        btnModeStart.addActionListener(e -> setMode(GridPanel.EditMode.START));
        btnModeGoal.addActionListener(e -> setMode(GridPanel.EditMode.GOAL));

        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnModeWall); p.add(btnModeErase);
        p.add(btnModeStart); p.add(btnModeGoal);
        highlightMode(GridPanel.EditMode.WALL);
        return p;
    }

    public FlatButton modeBtn(String text) {
        FlatButton b = new FlatButton(text, theme);
        b.setPreferredSize(new Dimension(80, 32));
        return b;
    }

    public void setMode(GridPanel.EditMode mode) {
        gridPanel.editMode = mode;
        highlightMode(mode);
    }

    public void highlightMode(GridPanel.EditMode mode) {
        Map<GridPanel.EditMode, FlatButton> map = Map.of(
                GridPanel.EditMode.WALL, btnModeWall,
                GridPanel.EditMode.ERASE, btnModeErase,
                GridPanel.EditMode.START, btnModeStart,
                GridPanel.EditMode.GOAL, btnModeGoal);
        map.forEach((m, b) -> b.setForeground(m == mode ? theme.accent : theme.btnText));
    }

    public JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(theme.subText);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    public void startSolve() {
        if (running.get()) return;
        if (gridPanel.startNode == null || gridPanel.goalNode == null) {
            setStatus("Set Robot Start Position and Target Location first.");
            return;
        }
        gridPanel.clearVisualization();
        gridPanel.repaint();
        paused.set(false);

        activeSolver = new AStarSolver(grid, gridPanel.startNode, gridPanel.goalNode);
        AStarSolver solver = activeSolver;

        running.set(true);
        btnSolve.setEnabled(false);
        long startTime = System.currentTimeMillis();

        worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                while (!solver.done && !isCancelled()) {
                    while (paused.get() && !isCancelled()) Thread.sleep(50);
                    solver.step();
                    Set<Node> openSnap = new HashSet<>(solver.openSetLookup);
                    Set<Node> closedSnap = new HashSet<>(solver.closedSet);
                    int steps = solver.steps;
                    SwingUtilities.invokeLater(() -> {
                        gridPanel.openSet = openSnap;
                        gridPanel.closedSet = closedSnap;
                        openSnap.forEach(n -> gridPanel.openFade.putIfAbsent(n, 1f));
                        closedSnap.forEach(n -> gridPanel.closedFade.putIfAbsent(n, 1f));
                        gridPanel.repaint();
                        metricsPanel.update("Running…", steps, -1, -1);
                    });
                    int delay = 201 - speedSlider.getValue();
                    Thread.sleep(Math.max(1, delay));
                }
                return null;
            }

            @Override protected void done() {
                long elapsed = System.currentTimeMillis() - startTime;
                running.set(false);
                btnSolve.setEnabled(true);
                if (solver.found) {
                    List<Node> path = solver.reconstructPath();
                    if (path == null) {
                        metricsPanel.update("No Path ✗", solver.steps, -1, elapsed);
                        setStatus("No feasible path found — robot cannot reach target.");
                        JOptionPane.showMessageDialog(AppFrame.this,
                                "<html>No feasible path found.<br>All possible nodes have been explored.<br>" +
                                "The robot cannot reach the target due to obstacles.</html>",
                                "Navigation Failed", JOptionPane.WARNING_MESSAGE);
                    } else {
                        gridPanel.path = path;
                        double cost = path.get(path.size() - 1).g;
                        metricsPanel.update("Path Found ✓", solver.steps, cost, elapsed, path.size());
                        setStatus("Optimal path found for robot navigation. Movement Cost: "
                                + String.format("%.2f", cost) + " | Nodes explored: " + solver.steps);
                        animateRobot(path);
                    }
                } else {
                    metricsPanel.update("No Path ✗", solver.steps, -1, elapsed);
                    setStatus("No feasible path found — robot cannot reach target.");
                    JOptionPane.showMessageDialog(AppFrame.this,
                            "<html>No feasible path found.<br>All possible nodes have been explored.<br>" +
                            "The robot cannot reach the target due to obstacles.</html>",
                            "Navigation Failed", JOptionPane.WARNING_MESSAGE);
                }
                gridPanel.repaint();
            }
        };
        worker.execute();
    }

    public void animateRobot(List<Node> path) {
        new SwingWorker<Void, Node>() {
            @Override protected Void doInBackground() throws Exception {
                for (Node n : path) {
                    publish(n);
                    int delay = Math.max(80, 201 - speedSlider.getValue());
                    Thread.sleep(delay);
                }
                return null;
            }
            @Override protected void process(List<Node> chunks) {
                gridPanel.robotCurrentPosition = chunks.get(chunks.size() - 1);
                gridPanel.repaint();
            }
            @Override protected void done() {
                gridPanel.robotCurrentPosition = null;
                gridPanel.repaint();
                setStatus("Robot reached the target!");
            }
        }.execute();
    }

    public void doStep() {
        if (activeSolver == null || activeSolver.done) return;
        paused.set(true);
        activeSolver.step();
        Set<Node> openSnap = new HashSet<>(activeSolver.openSetLookup);
        Set<Node> closedSnap = new HashSet<>(activeSolver.closedSet);
        gridPanel.openSet = openSnap;
        gridPanel.closedSet = closedSnap;
        openSnap.forEach(n -> gridPanel.openFade.putIfAbsent(n, 1f));
        closedSnap.forEach(n -> gridPanel.closedFade.putIfAbsent(n, 1f));
        metricsPanel.update("Stepped", activeSolver.steps, -1, -1);
        gridPanel.repaint();
    }

    public void placeRandomMaze() {
        if (gridPanel.startNode == null || gridPanel.goalNode == null) {
            setStatus("Place Robot Start Position and Target Location first, then generate obstacles.");
            return;
        }
        resetPath();
        Random rng = new Random();
        for (int attempt = 0; attempt < 50; attempt++) {
            for (Node[] row : grid.nodes)
                for (Node n : row) n.wall = false;
            for (Node[] row : grid.nodes)
                for (Node n : row)
                    if (!n.equals(gridPanel.startNode) && !n.equals(gridPanel.goalNode))
                        n.wall = rng.nextDouble() < 0.35;
            if (pathExists()) break;
        }
        gridPanel.repaint();
        setStatus("Obstacles generated. Press Start Navigation to run A*.");
    }

    public boolean pathExists() {
        Node src = gridPanel.startNode, dst = gridPanel.goalNode;
        if (src == null || dst == null) return false;
        Set<Node> visited = new HashSet<>();
        java.util.Queue<Node> queue = new java.util.ArrayDeque<>();
        queue.add(src); visited.add(src);
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            if (cur.equals(dst)) return true;
            for (Node nb : grid.neighbors(cur))
                if (!visited.contains(nb)) { visited.add(nb); queue.add(nb); }
        }
        return false;
    }

    public void resetPath() {
        if (worker != null) worker.cancel(true);
        running.set(false);
        paused.set(false);
        activeSolver = null;
        btnSolve.setEnabled(true);
        gridPanel.clearVisualization();
        grid.reset();
        gridPanel.repaint();
        metricsPanel.update("Idle", -1, -1, -1);
        setStatus("Navigation reset. Edit the environment and press Start Navigation.");
    }

    public void clearAll() {
        resetPath();
        for (Node[] row : grid.nodes)
            for (Node n : row) n.wall = false;
        gridPanel.startNode = null;
        gridPanel.goalNode = null;
        gridPanel.repaint();
        setStatus("Environment cleared. Place Robot Start Position, Target Location, and obstacles.");
    }

    public void changeGridSize() {
        GridSizeDialog dialog = new GridSizeDialog(this);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            int prevState = getExtendedState();
            Dimension prevSize = getSize();
            Point prevLocation = getLocation();

            if (worker != null) worker.cancel(true);
            running.set(false);
            paused.set(false);
            activeSolver = null;

            grid = new Grid(dialog.rows, dialog.cols);

            getContentPane().removeAll();
            JPanel root = new JPanel(new BorderLayout(0, 0)) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(theme.bg);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            root.setOpaque(false);
            setContentPane(root);

            root.add(buildHeader(), BorderLayout.NORTH);
            root.add(buildCenter(), BorderLayout.CENTER);
            root.add(buildSidebar(), BorderLayout.EAST);

            if ((prevState & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
                pack();
                setExtendedState(prevState);
            } else {
                pack();
                setSize(Math.max(prevSize.width, getPreferredSize().width),
                        Math.max(prevSize.height, getPreferredSize().height));
                setLocation(prevLocation);
            }

            revalidate();
            repaint();
            setStatus("Grid size changed. Place Robot Start Position and Target Location.");
        }
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    public void updateStatus() {
        boolean hasStart = gridPanel.startNode != null;
        boolean hasGoal = gridPanel.goalNode != null;
        if (!hasStart && !hasGoal) setStatus("Place Robot Start Position and Target Location.");
        else if (!hasStart) setStatus("Place Robot Start Position.");
        else if (!hasGoal) setStatus("Place Target Location.");
        else setStatus("Ready. Press Start Navigation to run A*.");
    }

    public void toggleTheme() {
        theme = theme.name.equals("Dark") ? Theme.light() : Theme.dark();
        btnTheme.setText(theme.name.equals("Dark") ? "☀ Light" : "☾ Dark");
        applyTheme();
    }

    public void applyTheme() {
        gridPanel.applyTheme(theme);
        metricsPanel.applyTheme(theme);
        legendPanel.applyTheme(theme);
        btnTheme.applyTheme(theme);
        btnSolve.applyTheme(theme); btnReset.applyTheme(theme);
        btnClear.applyTheme(theme); btnRandom.applyTheme(theme); btnChangeGrid.applyTheme(theme);
        btnPause.applyTheme(theme); btnResume.applyTheme(theme); btnStep.applyTheme(theme);
        btnModeWall.applyTheme(theme); btnModeErase.applyTheme(theme);
        btnModeStart.applyTheme(theme); btnModeGoal.applyTheme(theme);
        headerLabel.setForeground(theme.accent);
        descLabel.setForeground(theme.subText);
        statusLabel.setForeground(theme.subText);
        highlightMode(gridPanel.editMode);
        repaint();
    }
}
