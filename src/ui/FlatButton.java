package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FlatButton extends JButton {
    public Theme theme;
    public boolean hovered;

    public FlatButton(String text, Theme theme) {
        super(text);
        this.theme = theme;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
        });
    }

    public void applyTheme(Theme t) { this.theme = t; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? theme.btnHover : theme.btnBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.setColor(theme.btnText);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(getText())) / 2;
        int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), tx, ty);
    }

    @Override public Dimension getPreferredSize() { return new Dimension(130, 34); }
}
