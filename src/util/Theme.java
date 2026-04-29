package util;

import java.awt.Color;

public class Theme {
    public final String name;
    public final Color bg, panelBg, border, text, subText;
    public final Color cellEmpty, cellWall, cellStart, cellGoal;
    public final Color cellOpen, cellClosed, cellPath;
    public final Color btnBg, btnHover, btnText, accent;

    public Theme(String name,
            Color bg, Color panelBg, Color border, Color text, Color subText,
            Color cellEmpty, Color cellWall, Color cellStart, Color cellGoal,
            Color cellOpen, Color cellClosed, Color cellPath,
            Color btnBg, Color btnHover, Color btnText, Color accent) {
        this.name = name;
        this.bg = bg; this.panelBg = panelBg; this.border = border;
        this.text = text; this.subText = subText;
        this.cellEmpty = cellEmpty; this.cellWall = cellWall;
        this.cellStart = cellStart; this.cellGoal = cellGoal;
        this.cellOpen = cellOpen; this.cellClosed = cellClosed;
        this.cellPath = cellPath;
        this.btnBg = btnBg; this.btnHover = btnHover;
        this.btnText = btnText; this.accent = accent;
    }

    public static Theme dark() {
        return new Theme("Dark",
                new Color(18, 18, 24), new Color(28, 28, 38), new Color(50, 50, 70),
                new Color(220, 220, 235), new Color(130, 130, 160),
                new Color(38, 38, 52), new Color(20, 20, 28),
                new Color(0, 210, 120), new Color(255, 80, 100),
                new Color(30, 100, 200), new Color(80, 40, 120),
                new Color(255, 200, 0),
                new Color(55, 55, 80), new Color(75, 75, 110),
                new Color(220, 220, 235), new Color(80, 140, 255));
    }

    public static Theme light() {
        return new Theme("Light",
                new Color(240, 242, 248), new Color(255, 255, 255), new Color(200, 205, 220),
                new Color(30, 30, 50), new Color(100, 105, 130),
                new Color(250, 250, 255), new Color(60, 60, 80),
                new Color(0, 170, 90), new Color(220, 50, 70),
                new Color(70, 140, 230), new Color(160, 100, 200),
                new Color(240, 170, 0),
                new Color(220, 225, 240), new Color(200, 208, 230),
                new Color(30, 30, 50), new Color(60, 110, 220));
    }
}
