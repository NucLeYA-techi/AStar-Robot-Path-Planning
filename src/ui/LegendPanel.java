package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;

public class LegendPanel extends JPanel {
    public Theme theme;
    final String[] labels = {"Robot","Target","Obstacle","Frontier Nodes","Explored Nodes","Final Path"};

    public LegendPanel(Theme theme) {
        this.theme = theme;
        setOpaque(false);
        setLayout(new GridLayout(0, 2, 8, 6));
    }

    public Color colorFor(String label) {
        return switch (label) {
            case "Robot" -> theme.cellStart;
            case "Target" -> theme.cellGoal;
            case "Obstacle" -> theme.cellWall;
            case "Frontier Nodes" -> theme.cellOpen;
            case "Explored Nodes" -> theme.cellClosed;
            case "Final Path" -> theme.cellPath;
            default -> theme.cellEmpty;
        };
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        removeAll();
        for (String lbl : labels) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            row.setOpaque(false);
            JPanel swatch = new JPanel() {
                @Override protected void paintComponent(Graphics g2) {
                    Graphics2D g2d = (Graphics2D) g2;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(colorFor(lbl));
                    g2d.fillRoundRect(0, 0, 14, 14, 4, 4);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
            };
            swatch.setOpaque(false);
            JLabel txt = new JLabel(lbl);
            txt.setFont(new Font("SansSerif", Font.PLAIN, 11));
            txt.setForeground(theme.text);
            row.add(swatch); row.add(txt);
            add(row);
        }
        revalidate();
    }

    public void applyTheme(Theme t) { this.theme = t; repaint(); }
}
