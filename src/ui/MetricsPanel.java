package ui;

import util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MetricsPanel extends JPanel {
    public Theme theme;
    public JLabel lblStatus, lblNodes, lblCost, lblTime, lblPathLen, lblEfficiency;

    public MetricsPanel(Theme theme) {
        this.theme = theme;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        lblStatus     = metric("Status: Idle");
        lblNodes      = metric("Nodes Explored: —");
        lblCost       = metric("Movement Cost: —");
        lblTime       = metric("Computation Time: —");
        lblPathLen    = metric("Path Length: —");
        lblEfficiency = metric("Efficiency: —");

        for (JLabel l : new JLabel[]{lblStatus, lblNodes, lblCost, lblTime, lblPathLen, lblEfficiency}) {
            add(l);
            add(Box.createVerticalStrut(5));
        }
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Card background
        g2.setColor(new Color(theme.border.getRed(), theme.border.getGreen(),
                theme.border.getBlue(), 60));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        super.paintComponent(g);
    }

    private JLabel metric(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        // Foreground is set via applyTheme / constructor; use a safe dark default
        l.setForeground(theme.text != null ? theme.text : new Color(30, 30, 30));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    public void update(String status, int nodes, double cost, long ms) {
        update(status, nodes, cost, ms, -1);
    }

    public void update(String status, int nodes, double cost, long ms, int pathLength) {
        boolean isLight = theme.name.equals("Light");
        lblStatus.setText("Status: " + status);
        // Color-code status — ensure readable in both themes
        if (status.contains("✓") || status.contains("reached"))
            lblStatus.setForeground(isLight ? new Color(20, 130, 60) : theme.cellStart);
        else if (status.contains("✗") || status.contains("No Path"))
            lblStatus.setForeground(isLight ? new Color(180, 30, 30) : theme.cellGoal);
        else
            lblStatus.setForeground(isLight ? new Color(60, 80, 160) : theme.accent);

        lblNodes.setText("Nodes Explored: " + (nodes >= 0 ? nodes : "—"));
        lblCost.setText("Movement Cost: " + (cost >= 0 ? String.format("%.2f", cost) : "—"));
        lblTime.setText("Computation Time: " + (ms >= 0 ? ms + " ms" : "—"));
        lblPathLen.setText("Path Length: " + (pathLength >= 0 ? pathLength : "—"));
        if (pathLength >= 0 && nodes > 0) {
            double eff = pathLength * 100.0 / nodes;
            lblEfficiency.setText(String.format("Efficiency: %.1f%%", eff));
        } else {
            lblEfficiency.setText("Efficiency: —");
        }
    }

    public void applyTheme(Theme t) {
        this.theme = t;
        boolean isLight = t.name.equals("Light");
        Color fg = isLight ? new Color(25, 25, 40) : t.text;
        for (Component c : getComponents()) {
            if (c instanceof JLabel l) l.setForeground(fg);
        }
        // Re-color status label with accent (ensure it's readable in light too)
        Color accentFg = isLight ? new Color(60, 80, 160) : t.accent;
        lblStatus.setForeground(accentFg);
        repaint();
    }
}
