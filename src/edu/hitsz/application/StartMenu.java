package edu.hitsz.application;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
    private boolean ifMusicOn = false;

    // 添加回调接口
    public interface OnStartListener {
        void onStart(Difficulty difficulty, boolean soundOn);
    }

    private OnStartListener listener;

    public void setOnStartListener(OnStartListener listener) {
        this.listener = listener;
    }

    public StartMenu() {
        // 美化界面
        beautifyUI();

        // ------------------- 难度按钮事件 -------------------
        easyMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onStart(Difficulty.EASY, ifMusicOn);
                }
            }
        });

        commonMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onStart(Difficulty.NORMAL, ifMusicOn);
                }
            }
        });

        difficultMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onStart(Difficulty.HARD, ifMusicOn);
                }
            }
        });

        // ------------------- 音乐按钮事件 -------------------
        MusicBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MusicBotton.getText().equals("关")) {
                    MusicBotton.setText("开");
                    MusicBotton.setBackground(new Color(163, 204, 163)); // 浅绿色
                    ifMusicOn = true;
                } else {
                    MusicBotton.setText("关");
                    MusicBotton.setBackground(new Color(230, 176, 170)); // 浅红色
                    ifMusicOn = false;
                }
            }
        });
    }

    private void beautifyUI() {
        // 设置主面板背景 - 使用更浅的背景色
        MainPanel.setBackground(new Color(245, 245, 250)); // 浅蓝灰色背景
        MainPanel.setBorder(new EmptyBorder(40, 40, 40, 40)); // 内边距

        // 设置标题样式
        if (MainPanel.getComponentCount() > 0) {
            Component[] components = MainPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (label.getText() != null && !label.getText().isEmpty()) {
                        label.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
                        label.setForeground(new Color(60, 60, 80)); // 深灰色文字
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                }
            }
        }

        // 美化模式选择面板
        if (modeChoose != null) {
            modeChoose.setBackground(new Color(250, 250, 255));
            modeChoose.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(180, 200, 220), 2), // 浅蓝色边框
                    "选择难度",
                    TitledBorder.CENTER,
                    TitledBorder.TOP,
                    new Font("Microsoft YaHei", Font.BOLD, 18),
                    new Color(100, 100, 120) // 深灰色文字
            ));
        }

        // 美化音乐选择面板
        if (musicChoose != null) {
            musicChoose.setBackground(new Color(250, 250, 255));
            musicChoose.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(180, 200, 220), 2), // 浅蓝色边框
                    "音效设置",
                    TitledBorder.CENTER,
                    TitledBorder.TOP,
                    new Font("Microsoft YaHei", Font.BOLD, 18),
                    new Color(100, 100, 120) // 深灰色文字
            ));
        }

        // 美化音乐标签
        if (MusicChoose != null) {
            MusicChoose.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            MusicChoose.setForeground(new Color(80, 80, 100)); // 深灰色文字
        }

        // 美化按钮 - 使用浅色系
        beautifyButton(easyMode, new Color(180, 220, 180));   // 浅绿色
        beautifyButton(commonMode, new Color(180, 200, 230)); // 浅蓝色
        beautifyButton(difficultMode, new Color(230, 180, 180)); // 浅红色

        // 美化音乐按钮
        beautifyMusicButton(MusicBotton);
    }

    private void beautifyButton(JButton button, Color baseColor) {
        if (button == null) return;

        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        button.setForeground(new Color(60, 60, 80)); // 深灰色文字
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.brighter());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });

        // 设置按钮大小
        button.setPreferredSize(new Dimension(200, 50));
        button.setMinimumSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(200, 50));

        // 添加圆角效果
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseColor.darker(), 1), // 更细的边框
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
    }

    private void beautifyMusicButton(JButton button) {
        if (button == null) return;

        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        button.setForeground(new Color(60, 60, 80)); // 深灰色文字
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // 根据当前状态设置颜色
        if (button.getText().equals("开")) {
            button.setBackground(new Color(163, 204, 163)); // 浅绿色
        } else {
            button.setBackground(new Color(230, 176, 170)); // 浅红色
        }

        // 设置按钮大小
        button.setPreferredSize(new Dimension(80, 35));

        // 添加圆角效果
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200), 1), // 浅灰色边框
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Color current = button.getBackground();
                button.setBackground(current.brighter());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.getText().equals("开")) {
                    button.setBackground(new Color(163, 204, 163));
                } else {
                    button.setBackground(new Color(230, 176, 170));
                }
            }
        });
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("飞机大战 - 开始菜单");
        frame.setContentPane(new StartMenu().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null); // 居中显示
        frame.setVisible(true);
    }
}