package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;

public class MetricsPanel extends JPanel {
    public Theme theme;
    public JLabel lblNodes, lblCost, lblTime, lblStatus, lblPathLen, lblEfficiency;

    public MetricsPanel(Theme theme) {
        this.theme = theme;
        setOpaque(false);
        setLayout(new GridLayout(6, 1, 0, 6));
        lblStatus = metric("Status: Idle");
        lblNodes = metric("Nodes Explored (Search Space): —");
        lblCost = metric("Movement Cost: —");
        lblTime = metric("Computation Time: —");
        lblPathLen = metric("Path Length: —");
        lblEfficiency = metric("Efficiency: —");
        add(lblStatus); add(lblNodes); add(lblCost);
        add(lblTime); add(lblPathLen); add(lblEfficiency);
    }

    public JLabel metric(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        l.setForeground(theme.text);
        return l;
    }

    public void update(String status, int nodes, double cost, long ms) {
        update(status, nodes, cost, ms, -1);
    }

    public void update(String status, int nodes, double cost, long ms, int pathLength) {
        lblStatus.setText("Status: " + status);
        lblNodes.setText("Nodes Explored (Search Space): " + (nodes >= 0 ? nodes : "—"));
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
        for (Component c : getComponents())
            ((JLabel) c).setForeground(t.text);
    }
}
