package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GridSizeDialog extends JDialog {
    public int rows = 18, cols = 22;
    public boolean confirmed = false;

    public GridSizeDialog(JFrame parent) {
        super(parent, "Select Grid Size", true);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(25, 25, 25, 25));
        setResizable(false);

        JLabel titleLabel = new JLabel("Choose Grid Size", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(20,20,20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(5, 1, 12, 12));
        center.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton btn1 = createStyledButton("Small", "10 × 15", "Ideal for quick tests");
        JButton btn2 = createStyledButton("Medium", "18 × 22", "Balanced size (Default)");
        JButton btn3 = createStyledButton("Large", "25 × 30", "More complex paths");
        JButton btn4 = createStyledButton("Extra Large", "30 × 40", "Maximum complexity");
        JButton btnCustom = createStyledButton("Custom Size", "Your choice", "Up to 30 × 40");

        btn1.addActionListener(e -> { rows = 10; cols = 15; confirmed = true; dispose(); });
        btn2.addActionListener(e -> { rows = 18; cols = 22; confirmed = true; dispose(); });
        btn3.addActionListener(e -> { rows = 25; cols = 30; confirmed = true; dispose(); });
        btn4.addActionListener(e -> { rows = 30; cols = 40; confirmed = true; dispose(); });
        btnCustom.addActionListener(e -> showCustomDialog());

        center.add(btn1); center.add(btn2); center.add(btn3); center.add(btn4); center.add(btnCustom);
        add(center, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public JButton createStyledButton(String title, String size, String desc) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(8, 4));
        btn.setPreferredSize(new Dimension(350, 60));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title + " - " + size);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLbl.setForeground(new Color(60, 60, 60));
        descLbl.setAlignmentX(LEFT_ALIGNMENT);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(descLbl);

        btn.add(textPanel, BorderLayout.CENTER);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(25,25,25));
        btn.setBorder(new CompoundBorder(
        new LineBorder(new Color(180,180,180), 1),
        new EmptyBorder(8,12,8,12)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(235,240,255));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            btn.setBackground(Color.WHITE);
        }
    });
        return btn;
    }

    public void showCustomDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField rowField = new JTextField("18", 10);
        JTextField colField = new JTextField("22", 10);
        rowField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        colField.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel rowLbl = new JLabel("Rows (max 30):");
        JLabel colLbl = new JLabel("Columns (max 40):");
        rowLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        colLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.add(rowLbl); panel.add(rowField);
        panel.add(colLbl); panel.add(colField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Custom Grid Size",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int r = Integer.parseInt(rowField.getText().trim());
                int c = Integer.parseInt(colField.getText().trim());
                if (r > 0 && r <= 30 && c > 0 && c <= 40) {
                    rows = r; cols = c; confirmed = true; dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter rows (1-30) and columns (1-40). Try again.",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    showCustomDialog();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers. Try again.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                showCustomDialog();
            }
        }
    }
}
