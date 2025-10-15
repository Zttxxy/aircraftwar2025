package edu.hitsz.application;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartMenu {
    private JButton easyMode;
    private JButton commonMode;
    private JButton difficultMode;
    private JPanel musicChoose;
    private JPanel modeChoose;
    private JButton MusicBotton;
    private JLabel MusicChoose;
    private JPanel MainPanel;
    private Game game;
    private boolean ifMusicOn = false;

    public StartMenu() {

        // ------------------- 难度按钮事件 -------------------
        easyMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game = new EasyGame(); // EasyGame 子类
                showGamePanel();       // ✅ 这里使用已经创建好的 game
            }
        });

        commonMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game = new CommonGame(); // CommonGame 子类
                showGamePanel();         // ✅ 这里使用已经创建好的 game
            }
        });

        difficultMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game = new DifficultGame(); // DifficultGame 子类
                showGamePanel();            // ✅ 这里使用已经创建好的 game
            }
        });

        // ------------------- 音乐按钮事件 -------------------
        MusicBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MusicBotton.getText().equals("关")) {
                    MusicBotton.setText("开");
                    ifMusicOn = true;
                } else {
                    MusicBotton.setText("关");
                    ifMusicOn = false;
                }
            }
        });
    }

    // ------------------- 显示游戏面板的方法 -------------------
    public void showGamePanel() {

        //  传入音乐开关
        if (ifMusicOn && game != null) {
            game.ifMusicOn = true;
            MusicThread bgm = new MusicThread("src/resources/bgm.wav");
            bgm.setLoop(true); // 循环播放
            bgm.start();
            game.setBgmThread(bgm); // 需要在 Game 中保存引用，用于游戏结束时停止播放
        }

        // 添加到主卡片面板
        Main.cardPanel.add(game);
        Main.cardLayout.last(Main.cardPanel);
        game.action();
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("StartMenu");
        frame.setContentPane(new StartMenu().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
