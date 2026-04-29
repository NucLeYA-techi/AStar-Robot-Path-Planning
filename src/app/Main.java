package app;

import ui.AppFrame;
import ui.GridSizeDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            AppFrame frame = new AppFrame(18, 22);

            GridSizeDialog dialog = new GridSizeDialog(frame);
            dialog.setVisible(true);

            if (dialog.confirmed) {
                frame.dispose();
                new AppFrame(dialog.rows, dialog.cols);
            } else {
                frame.dispose();
                System.exit(0);
            }
        });
    }
}
