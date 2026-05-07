package util;

import java.awt.Color;

public class Theme {
    public final String name;
    public final Color bg, panelBg, border, text, subText;
    public final Color cellEmpty, cellWall, cellStart, cellGoal;
    public final Color cellOpen, cellClosed, cellPath;
    public final Color btnBg, btnHover, btnText, accent;
    /** Color used for the robot's current cell during animation — distinct from path. */
    public final Color cellRobot;

    public Theme(String name,
            Color bg, Color panelBg, Color border, Color text, Color subText,
            Color cellEmpty, Color cellWall, Color cellStart, Color cellGoal,
            Color cellOpen, Color cellClosed, Color cellPath, Color cellRobot,
            Color btnBg, Color btnHover, Color btnText, Color accent) {
        this.name = name;
        this.bg = bg; this.panelBg = panelBg; this.border = border;
        this.text = text; this.subText = subText;
        this.cellEmpty = cellEmpty; this.cellWall = cellWall;
        this.cellStart = cellStart; this.cellGoal = cellGoal;
        this.cellOpen = cellOpen; this.cellClosed = cellClosed;
        this.cellPath = cellPath; this.cellRobot = cellRobot;
        this.btnBg = btnBg; this.btnHover = btnHover;
        this.btnText = btnText; this.accent = accent;
    }

    public static Theme dark() {
        return new Theme("Dark",
            new Color(18, 18, 24),        // bg
            new Color(28, 28, 38),        // panelBg
            new Color(50, 50, 70),        // border
            new Color(220, 220, 235),     // text
            new Color(160, 160, 190),     // subText
            new Color(38, 38, 52),        // cellEmpty
            new Color(22, 22, 32),        // cellWall — near-black
            new Color(0, 210, 120),       // cellStart — bright green
            new Color(255, 75, 95),       // cellGoal — vivid red
            new Color(45, 115, 235),      // cellOpen — bright blue frontier
            new Color(130, 55, 195),      // cellClosed — vivid violet explored
            new Color(255, 205, 30),      // cellPath — bright gold
            new Color(0, 230, 255),       // cellRobot — electric cyan
            new Color(55, 55, 80),        // btnBg
            new Color(75, 75, 115),       // btnHover
            new Color(220, 220, 235),     // btnText
            new Color(80, 140, 255));     // accent
    }

    public static Theme light() {
        return new Theme("Light",
            new Color(232, 235, 244),     // bg
            new Color(245, 245, 250),     // panelBg
            new Color(130, 135, 160),     // border
            new Color(25, 25, 25),        // text
            new Color(48, 52, 78),        // subText
            new Color(237, 240, 252),     // cellEmpty
            new Color(50, 50, 70),        // cellWall — dark slate
            new Color(0, 155, 85),        // cellStart — deep green
            new Color(200, 35, 55),       // cellGoal — deep red
            new Color(40, 110, 210),      // cellOpen — deep blue frontier
            new Color(130, 55, 190),      // cellClosed — vivid violet explored
            new Color(210, 130, 0),       // cellPath — deep amber-orange
            new Color(0, 180, 205),       // cellRobot — cyan
            new Color(210, 215, 235),     // btnBg
            new Color(190, 200, 230),     // btnHover
            Color.BLACK,                  // btnText
            new Color(70, 90, 170));      // accent
    }
}
