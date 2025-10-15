package edu.hitsz.application;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 768;

    static final CardLayout cardLayout = new CardLayout();
    static final JPanel cardPanel = new JPanel(cardLayout);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Aircraft War");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 居中
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((int)(screenSize.getWidth() - WINDOW_WIDTH)/2, 0,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        frame.add(cardPanel);
        frame.setVisible(true);

        // 显示开始菜单
        StartMenu startMenu = new StartMenu();
        cardPanel.add(startMenu.getMainPanel());
        cardLayout.show(cardPanel, "StartMenu");
    }
}
