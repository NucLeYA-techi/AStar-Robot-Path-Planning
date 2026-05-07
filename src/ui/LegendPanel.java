package ui;

import util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LegendPanel extends JPanel {
    public Theme theme;
    private static final String[] LABELS = {
        "Start", "Goal", "Obstacle",
        "Frontier", "Explored", "Final Path", "Robot"
    };

    public LegendPanel(Theme theme) {
        this.theme = theme;
        setOpaque(false);
        setLayout(new GridLayout(0, 2, 8, 5));
        setBorder(new EmptyBorder(8, 10, 8, 10));
        rebuild();
    }

    private Color colorFor(String label) {
        return switch (label) {
            case "Start"     -> theme.cellStart;
            case "Goal"      -> theme.cellGoal;
            case "Obstacle"  -> theme.cellWall;
            case "Frontier"  -> theme.cellOpen;
            case "Explored"  -> theme.cellClosed;
            case "Final Path"-> theme.cellPath;
            case "Robot"     -> theme.cellRobot;
            default          -> theme.cellEmpty;
        };
    }

    private void rebuild() {
        removeAll();
        boolean isLight = theme.name.equals("Light");
        for (String lbl : LABELS) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row.setOpaque(false);

            Color c = colorFor(lbl);
            JPanel swatch = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, 14, 14, 4, 4);
                    g2.setColor(isLight ? new Color(0, 0, 0, 60) : new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, 13, 13, 4, 4);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
            };
            swatch.setOpaque(false);

            JLabel txt = new JLabel(lbl);
            txt.setFont(new Font("SansSerif", Font.PLAIN, 12));
            txt.setForeground(isLight ? new Color(25, 25, 40) : theme.text);

            row.add(swatch);
            row.add(txt);
            add(row);
        }
        revalidate();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(theme.border.getRed(), theme.border.getGreen(),
                theme.border.getBlue(), 60));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        super.paintComponent(g);
    }

    public void applyTheme(Theme t) { this.theme = t; rebuild(); repaint(); }
}
