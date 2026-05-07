package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FlatButton extends JButton {
    public Theme theme;
    private boolean hovered;
    private boolean pressed;

    public FlatButton(String text, Theme theme) {
        super(text);
        this.theme = theme;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { hovered = true;  pressed = false; repaint(); }
            public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
            public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
            public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    public void applyTheme(Theme t) { this.theme = t; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = 10;
        boolean isLight = theme.name.equals("Light");

        // Drop shadow (dark mode only — light mode uses border instead)
        if (!pressed && isEnabled() && !isLight) {
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(2, 3, w - 4, h - 2, arc, arc);
        }

        // Background
        Color bg;
        if (!isEnabled())     bg = isLight ? new Color(200, 202, 215) : new Color(40, 40, 55);
        else if (pressed)     bg = isLight ? new Color(190, 195, 215) : theme.btnBg.darker();
        else if (hovered)     bg = isLight ? new Color(210, 215, 235) : theme.btnHover;
        else                  bg = isLight ? new Color(220, 224, 242) : theme.btnBg;
        g2.setColor(bg);
        g2.fillRoundRect(1, 1, w - 3, h - 3, arc, arc);

        // Border — visible in light mode for definition, subtle in dark
        if (isLight) {
            g2.setColor(isEnabled() ? new Color(115, 123, 150) : new Color(148, 154, 178));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
        } else if (!pressed && isEnabled()) {
            g2.setColor(new Color(255, 255, 255, 15));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
        }
        g2.setStroke(new BasicStroke(1f));

        // Text
        Color textColor;
        if (!isEnabled()) {
            textColor = isLight ? new Color(120, 120, 130) : new Color(100, 100, 130);
        } else if (isLight) {
            textColor = new Color(25, 25, 40);   // near-black for light theme
        } else {
            textColor = getForeground();
        }
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);
        g2.drawString(getText(), tx, ty);
    }

    @Override public Dimension getPreferredSize() { return new Dimension(130, 34); }
}
