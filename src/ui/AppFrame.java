package ui;

import model.*;
import util.ScenarioManager;
import util.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppFrame extends JFrame {
    public Theme theme;
    public Grid grid;
    public GridPanel gridPanel;
    public MetricsPanel metricsPanel;
    public LegendPanel legendPanel;

    public FlatButton btnSolve, btnTheme, btnRandom, btnChangeGrid;
    public FlatButton btnPause, btnResume, btnStep;
    public FlatButton btnModeWall, btnModeErase, btnModeStart, btnModeGoal;
    // Improved reset controls
    public FlatButton btnClearPath, btnClearObstacles, btnFullReset;
    // New: Save Simulation
    public FlatButton btnSave;
    // New: Scenario dropdown
    public JComboBox<ScenarioManager.Scenario> scenarioCombo;

    /** Tracks the currently active scenario so Generate Obstacles stays themed. */
    private ScenarioManager.Scenario activeScenario = null;

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

        statusLabel = new JLabel("Click the grid to place the Start node (S).");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(theme.text);

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

        // ── Preset Scenarios ──────────────────────────────────────────────
        side.add(sectionLabel("Preset Scenario"));
        side.add(Box.createVerticalStrut(6));
        scenarioCombo = new JComboBox<>(ScenarioManager.Scenario.values());
        scenarioCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        scenarioCombo.setAlignmentX(LEFT_ALIGNMENT);
        scenarioCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        styleCombo(scenarioCombo);
        side.add(scenarioCombo);
        side.add(Box.createVerticalStrut(5));
        FlatButton btnApplyScenario = new FlatButton("⊞ Apply Scenario", theme);
        btnApplyScenario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnApplyScenario.setAlignmentX(LEFT_ALIGNMENT);
        btnApplyScenario.addActionListener(e -> applyScenario());
        side.add(btnApplyScenario);
        side.add(Box.createVerticalStrut(14));

        // ── Edit Mode ─────────────────────────────────────────────────────
        side.add(sectionLabel("Edit Mode"));
        side.add(Box.createVerticalStrut(6));
        side.add(buildModeButtons());
        side.add(Box.createVerticalStrut(14));

        // ── Animation Speed ───────────────────────────────────────────────
        side.add(sectionLabel("Animation Speed"));
        side.add(Box.createVerticalStrut(6));
        speedSlider = new JSlider(1, 200, 40);
        speedSlider.setOpaque(false);
        speedSlider.setForeground(theme.text);
        side.add(speedSlider);
        side.add(Box.createVerticalStrut(14));

        // ── Navigation Controls ───────────────────────────────────────────
        side.add(sectionLabel("Navigation"));
        side.add(Box.createVerticalStrut(6));
        btnSolve   = new FlatButton("▶ Start Navigation", theme);
        btnPause   = new FlatButton("⏸ Pause", theme);
        btnResume  = new FlatButton("▶ Resume", theme);
        btnStep    = new FlatButton("⏭ Step", theme);
        for (FlatButton b : new FlatButton[]{btnSolve, btnPause, btnResume, btnStep}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            b.setAlignmentX(LEFT_ALIGNMENT);
            side.add(b);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalStrut(4));

        // ── Reset Controls ────────────────────────────────────────────────
        side.add(sectionLabel("Reset"));
        side.add(Box.createVerticalStrut(6));
        btnClearPath      = new FlatButton("✕ Clear Path", theme);
        btnClearObstacles = new FlatButton("⬜ Clear Obstacles", theme);
        btnFullReset      = new FlatButton("↺ Full Reset", theme);
        for (FlatButton b : new FlatButton[]{btnClearPath, btnClearObstacles, btnFullReset}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            b.setAlignmentX(LEFT_ALIGNMENT);
            side.add(b);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalStrut(4));

        // ── Utilities ─────────────────────────────────────────────────────
        side.add(sectionLabel("Utilities"));
        side.add(Box.createVerticalStrut(6));
        btnRandom     = new FlatButton("⚄ Generate Obstacles", theme);
        btnSave       = new FlatButton("💾 Save Simulation", theme);
        btnChangeGrid = new FlatButton("⊞ Change Grid Size", theme);
        for (FlatButton b : new FlatButton[]{btnRandom, btnSave, btnChangeGrid}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            b.setAlignmentX(LEFT_ALIGNMENT);
            side.add(b);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalStrut(8));

        // ── Metrics ───────────────────────────────────────────────────────
        side.add(sectionLabel("Metrics"));
        side.add(Box.createVerticalStrut(6));
        metricsPanel = new MetricsPanel(theme);
        metricsPanel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(metricsPanel);
        side.add(Box.createVerticalStrut(14));

        // ── Legend ────────────────────────────────────────────────────────
        side.add(sectionLabel("Legend"));
        side.add(Box.createVerticalStrut(6));
        legendPanel = new LegendPanel(theme);
        legendPanel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(legendPanel);
        side.add(Box.createVerticalGlue());

        // Wire up actions
        btnSolve.addActionListener(e -> startSolve());
        btnPause.addActionListener(e -> { paused.set(true); setStatus("Navigation paused."); });
        btnResume.addActionListener(e -> { paused.set(false); setStatus("Searching for optimal route..."); });
        btnStep.addActionListener(e -> doStep());
        btnClearPath.addActionListener(e -> clearPath());
        btnClearObstacles.addActionListener(e -> clearObstacles());
        btnFullReset.addActionListener(e -> fullReset());
        btnRandom.addActionListener(e -> placeRandomMaze());
        btnSave.addActionListener(e -> saveSimulation());
        btnChangeGrid.addActionListener(e -> changeGridSize());

        return side;
    }

    public JPanel buildModeButtons() {
        btnModeWall  = modeBtn("✏ Obstacle");
        btnModeErase = modeBtn("⌫ Erase");
        btnModeStart = modeBtn("S Robot");
        btnModeGoal  = modeBtn("G Target");

        btnModeWall.addActionListener(e  -> setMode(GridPanel.EditMode.WALL));
        btnModeErase.addActionListener(e -> setMode(GridPanel.EditMode.ERASE));
        btnModeStart.addActionListener(e -> setMode(GridPanel.EditMode.START));
        btnModeGoal.addActionListener(e  -> setMode(GridPanel.EditMode.GOAL));

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
                GridPanel.EditMode.WALL,  btnModeWall,
                GridPanel.EditMode.ERASE, btnModeErase,
                GridPanel.EditMode.START, btnModeStart,
                GridPanel.EditMode.GOAL,  btnModeGoal);
        map.forEach((m, b) -> b.setForeground(m == mode ? theme.accent : theme.btnText));
    }

    public JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(theme.name.equals("Light") ? new Color(30, 30, 30) : theme.subText);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ── Scenario ──────────────────────────────────────────────────────────

    public void applyScenario() {
        if (running.get()) return;
        ScenarioManager.Scenario selected =
                (ScenarioManager.Scenario) scenarioCombo.getSelectedItem();
        if (selected == null) return;

        if (worker != null) worker.cancel(true);
        running.set(false); paused.set(false); activeSolver = null;
        btnSolve.setEnabled(true);

        activeScenario = selected;
        ScenarioManager.ScenarioResult result = ScenarioManager.apply(selected, grid);
        gridPanel.clearVisualization();

        Node s = grid.get(result.startRow(), result.startCol());
        Node g = grid.get(result.goalRow(), result.goalCol());
        if (s != null && !s.wall) gridPanel.startNode = s;
        if (g != null && !g.wall) gridPanel.goalNode  = g;

        // Scenario provides start+goal — skip guided flow
        gridPanel.markSetupComplete();

        gridPanel.repaint();
        metricsPanel.update("Idle", -1, -1, -1);
        setStatus("Scenario \"" + selected.label + "\" loaded. Press Start Navigation.");
    }

    // ── Navigation ────────────────────────────────────────────────────────

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
        setStatus("Searching for optimal route...");
        long startTime = System.currentTimeMillis();

        worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                while (!solver.done && !isCancelled()) {
                    while (paused.get() && !isCancelled()) Thread.sleep(50);
                    solver.step();
                    Set<Node> openSnap   = new HashSet<>(solver.openSetLookup);
                    Set<Node> closedSnap = new HashSet<>(solver.closedSet);
                    int steps = solver.steps;
                    SwingUtilities.invokeLater(() -> {
                        gridPanel.openSet   = openSnap;
                        gridPanel.closedSet = closedSnap;
                        openSnap.forEach(n   -> gridPanel.openFade.putIfAbsent(n, 1f));
                        closedSnap.forEach(n -> gridPanel.closedFade.putIfAbsent(n, 1f));
                        gridPanel.repaint();
                        metricsPanel.update("Exploring nodes...", steps, -1, -1);
                        setStatus("Exploring nodes... (" + steps + " explored)");
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
                        setStatus("No valid path exists — robot cannot reach target.");
                        showNoPathDialog();
                    } else {
                        gridPanel.path = path;
                        double cost = path.get(path.size() - 1).g;
                        metricsPanel.update("Path Found ✓", solver.steps, cost, elapsed, path.size());
                        setStatus("Path found! Cost: " + String.format("%.2f", cost)
                                + " | Nodes explored: " + solver.steps);
                        animateRobot(path);
                    }
                } else {
                    metricsPanel.update("No Path ✗", solver.steps, -1, elapsed);
                    setStatus("No valid path exists — robot cannot reach target.");
                    showNoPathDialog();
                }
                gridPanel.repaint();
            }
        };
        worker.execute();
    }

    private void showNoPathDialog() {
        JOptionPane.showMessageDialog(this,
                "<html>No feasible path found.<br>All possible nodes have been explored.<br>" +
                "The robot cannot reach the target due to obstacles.</html>",
                "Navigation Failed", JOptionPane.WARNING_MESSAGE);
    }

    public void animateRobot(List<Node> path) {
        setStatus("Robot is moving along the optimal path...");
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
                gridPanel.robotCurrentPosition = path.get(path.size() - 1);
                gridPanel.repaint();
                setStatus("Robot reached the target! ✓");
            }
        }.execute();
    }

    public void doStep() {
        if (activeSolver == null || activeSolver.done) return;
        paused.set(true);
        activeSolver.step();
        Set<Node> openSnap   = new HashSet<>(activeSolver.openSetLookup);
        Set<Node> closedSnap = new HashSet<>(activeSolver.closedSet);
        gridPanel.openSet   = openSnap;
        gridPanel.closedSet = closedSnap;
        openSnap.forEach(n   -> gridPanel.openFade.putIfAbsent(n, 1f));
        closedSnap.forEach(n -> gridPanel.closedFade.putIfAbsent(n, 1f));
        metricsPanel.update("Stepped", activeSolver.steps, -1, -1);
        setStatus("Step executed. Nodes explored: " + activeSolver.steps);
        gridPanel.repaint();
    }

    // ── Reset Controls ────────────────────────────────────────────────────

    /** Removes explored nodes and path only; keeps obstacles and start/goal. */
    public void clearPath() {
        if (worker != null) worker.cancel(true);
        running.set(false); paused.set(false); activeSolver = null;
        btnSolve.setEnabled(true);
        gridPanel.clearVisualization();
        grid.reset();
        gridPanel.repaint();
        metricsPanel.update("Idle", -1, -1, -1);
        setStatus("Path cleared. Obstacles and positions preserved.");
    }

    /** Removes all walls only; keeps start/goal and any path visualization. */
    public void clearObstacles() {
        for (Node[] row : grid.nodes)
            for (Node n : row) n.wall = false;
        gridPanel.repaint();
        setStatus("Obstacles cleared. Start/Goal positions preserved.");
    }

    /** Resets everything: path, obstacles, start, goal. */
    public void fullReset() {
        if (worker != null) worker.cancel(true);
        running.set(false); paused.set(false); activeSolver = null;
        btnSolve.setEnabled(true);
        activeScenario = null;
        gridPanel.clearVisualization();
        grid.reset();
        for (Node[] row : grid.nodes)
            for (Node n : row) n.wall = false;
        gridPanel.resetSetup();   // clears startNode, goalNode, resets guided flow
        gridPanel.repaint();
        metricsPanel.update("Idle", -1, -1, -1);
        setStatus("Click the grid to place the Start node.");
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    /**
     * Regenerates obstacles using the currently active scenario style.
     * Falls back to random if no scenario has been applied yet.
     */
    public void placeRandomMaze() {
        if (running.get()) return;

        if (activeScenario != null) {
            // Re-run the scenario generator with a fresh random seed
            if (worker != null) worker.cancel(true);
            running.set(false); paused.set(false); activeSolver = null;
            btnSolve.setEnabled(true);

            long seed = System.currentTimeMillis();
            ScenarioManager.ScenarioResult result =
                    ScenarioManager.applyWithSeed(activeScenario, grid, seed);
            gridPanel.clearVisualization();

            // Preserve existing start/goal if still on open cells, else use suggested
            if (gridPanel.startNode == null || gridPanel.startNode.wall) {
                Node s = grid.get(result.startRow(), result.startCol());
                if (s != null && !s.wall) gridPanel.startNode = s;
            }
            if (gridPanel.goalNode == null || gridPanel.goalNode.wall) {
                Node g = grid.get(result.goalRow(), result.goalCol());
                if (g != null && !g.wall) gridPanel.goalNode = g;
            }

            gridPanel.markSetupComplete();
            gridPanel.repaint();
            setStatus("New \"" + activeScenario.label + "\" layout generated.");
            return;
        }

        // No scenario selected — require start/goal first, then do random
        if (gridPanel.startNode == null || gridPanel.goalNode == null) {
            setStatus("Apply a scenario or place Start/Goal first.");
            return;
        }
        clearPath();
        Random rng = new Random();
        for (int attempt = 0; attempt < 50; attempt++) {
            for (Node[] row : grid.nodes) for (Node n : row) n.wall = false;
            for (Node[] row : grid.nodes)
                for (Node n : row)
                    if (!n.equals(gridPanel.startNode) && !n.equals(gridPanel.goalNode))
                        n.wall = rng.nextDouble() < 0.30;
            if (pathExists()) break;
        }
        gridPanel.repaint();
        setStatus("Random obstacles generated. Press Start Navigation.");
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

    /** Saves the current grid visualization as a PNG file. */
    public void saveSimulation() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Simulation as PNG");
        chooser.setSelectedFile(new File("simulation.png"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Image", "png"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png"))
            file = new File(file.getAbsolutePath() + ".png");

        try {
            BufferedImage img = gridPanel.toImage();
            ImageIO.write(img, "PNG", file);
            setStatus("Simulation saved to: " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save image: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void changeGridSize() {
        GridSizeDialog dialog = new GridSizeDialog(this);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            int prevState = getExtendedState();
            Dimension prevSize = getSize();
            Point prevLocation = getLocation();

            if (worker != null) worker.cancel(true);
            running.set(false); paused.set(false); activeSolver = null;

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

    // ── Status & Theme ────────────────────────────────────────────────────

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    public void updateStatus() {
        switch (gridPanel.setupState) {
            case NEED_START -> setStatus("Click the grid to place the Start node (S).");
            case NEED_GOAL  -> setStatus("Start placed. Now click to place the Goal node (G).");
            case FREE -> {
                boolean hasStart = gridPanel.startNode != null;
                boolean hasGoal  = gridPanel.goalNode  != null;
                if (!hasStart && !hasGoal) setStatus("Click the grid to place the Start node (S).");
                else if (!hasStart)        setStatus("Place the Start node (S).");
                else if (!hasGoal)         setStatus("Place the Goal node (G).");
                else                       setStatus("Ready. Press Start Navigation to run A*.");
            }
        }
    }

    /** Fully styles a JComboBox for the current theme, bypassing LAF color interference. */
    private void styleCombo(JComboBox<?> combo) {
        boolean isLight = theme.name.equals("Light");
        Color bg  = isLight ? new Color(220, 224, 242) : theme.btnBg;
        Color fg  = Color.WHITE;

        combo.setBackground(bg);
        combo.setForeground(fg);
        combo.setOpaque(true);

        // Force the editor component (the visible text field part) to match
        Component editor = combo.getEditor().getEditorComponent();
        if (editor instanceof JComponent jc) {
            jc.setBackground(bg);
            jc.setForeground(fg);
            jc.setOpaque(true);
        }

        // Renderer covers both the closed display row (index == -1) and open list items
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                boolean light = theme.name.equals("Light");
                Color itemBg = light ? new Color(220, 224, 242) : theme.btnBg;
                Color itemFg = Color.WHITE;
                if (isSelected && index >= 0) {          // highlighted in open list
                    setBackground(theme.accent);
                    setForeground(Color.WHITE);
                } else {                                  // closed display + unselected items
                    setBackground(itemBg);
                    setForeground(itemFg);
                }
                setOpaque(true);
                return this;
            }
        });

        combo.repaint();
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

        styleCombo(scenarioCombo);
        speedSlider.setForeground(theme.text);

        for (FlatButton b : new FlatButton[]{
                btnTheme, btnSolve, btnClearPath, btnClearObstacles, btnFullReset,
                btnRandom, btnSave, btnChangeGrid,
                btnPause, btnResume, btnStep,
                btnModeWall, btnModeErase, btnModeStart, btnModeGoal}) {
            b.applyTheme(theme);
        }

        headerLabel.setForeground(theme.accent);
        descLabel.setForeground(theme.subText);
        statusLabel.setForeground(theme.text);   // use full text color, not subText
        highlightMode(gridPanel.editMode);
        repaint();
    }
}
