package edu.hitsz.application;

import edu.hitsz.aircraft.HeroAircraft;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 768;

    // 添加静态字段
    public static JPanel cardPanel;
    public static CardLayout cardLayout;

    public static void main(String[] args) {
        // 初始化主窗口
        JFrame frame = new JFrame("Aircraft War");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建卡片布局
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 创建开始菜单
        StartMenu startMenu = new StartMenu();
        startMenu.setOnStartListener(new StartMenu.OnStartListener() {
            @Override
            public void onStart(Difficulty difficulty, boolean soundOn) {
                // 创建对应难度的游戏
                Game game;
                switch (difficulty) {
                    case EASY:
                        game = new EasyGame();
                        break;
                    case NORMAL:
                        game = new CommonGame();
                        break;
                    case HARD:
                        game = new DifficultGame();
                        break;
                    default:
                        game = new CommonGame();
                }

                // 设置音效
                game.ifMusicOn = soundOn;

                // 添加游戏到卡片面板
                game.setName("game");
                cardPanel.add(game, "game");

                // 显示游戏
                cardLayout.show(cardPanel, "game");

                // 开始游戏
                game.action();
            }
        });

        // 添加开始菜单到卡片面板
        cardPanel.add(startMenu.getMainPanel(), "menu");

        // 设置主窗口内容
        frame.setContentPane(cardPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}