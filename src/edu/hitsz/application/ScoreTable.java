package edu.hitsz.application;

import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScoreTable {
    private JPanel MainPanel;
    private JPanel TopPanel;
    private JPanel BottomPanel;
    private JLabel head;
    private JLabel mode;
    private JScrollPane scoreScroll;
    private JTable scoreList;
    private JButton deleteButton;
    private JButton returnBotton;

    private DefaultTableModel tableModel;
    private PlayerDao playerDao;
    private Difficulty difficulty;

    // 颜色主题
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color PANEL_BACKGROUND = new Color(255, 255, 255);
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(100, 149, 237);
    private final Color ACCENT_COLOR = new Color(255, 99, 71);
    private final Color TEXT_COLOR = new Color(60, 60, 80);
    private final Color TABLE_HEADER_COLOR = new Color(230, 230, 250);
    private final Color TABLE_ROW_COLOR1 = new Color(248, 248, 255);
    private final Color TABLE_ROW_COLOR2 = new Color(240, 248, 255);
    private final Color SELECTION_COLOR = new Color(173, 216, 230);

    public ScoreTable(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.playerDao = new PlayerDaoImpl();

        initializeUI();
        loadRecords();
        setupListeners();
    }

    private void initializeUI() {
        // 设置主面板背景
        MainPanel.setBackground(BACKGROUND_COLOR);
        MainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 美化顶部面板
        TopPanel.setBackground(PANEL_BACKGROUND);
        TopPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // 美化标题
        head.setFont(new Font("微软雅黑", Font.BOLD, 28));
        head.setForeground(PRIMARY_COLOR);
        head.setHorizontalAlignment(SwingConstants.CENTER);

        // 美化难度标签
        mode.setText("难度: " + getDifficultyName(difficulty));
        mode.setFont(new Font("微软雅黑", Font.BOLD, 18));
        mode.setForeground(SECONDARY_COLOR);
        mode.setHorizontalAlignment(SwingConstants.CENTER);

        // 初始化表格
        String[] columnNames = {"排名", "玩家名称", "得分", "时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        scoreList.setModel(tableModel);

        // 美化表格
        beautifyTable();

        // 美化底部面板
        BottomPanel.setBackground(PANEL_BACKGROUND);
        BottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // 美化按钮
        beautifyButton(deleteButton, ACCENT_COLOR);
        beautifyButton(returnBotton, SECONDARY_COLOR);
    }

    private void beautifyTable() {
        // 设置表格样式
        scoreList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        scoreList.setRowHeight(35);
        scoreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoreList.setShowGrid(true);
        scoreList.setGridColor(new Color(220, 220, 220));
        scoreList.setIntercellSpacing(new Dimension(0, 0));

        // 设置表格背景
        scoreList.setBackground(Color.WHITE);
        scoreList.setForeground(TEXT_COLOR);

        // 设置表头
        JTableHeader header = scoreList.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 16));
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // 设置列宽
        scoreList.getColumnModel().getColumn(0).setPreferredWidth(80);  // 排名
        scoreList.getColumnModel().getColumn(1).setPreferredWidth(150); // 玩家名称
        scoreList.getColumnModel().getColumn(2).setPreferredWidth(100); // 得分
        scoreList.getColumnModel().getColumn(3).setPreferredWidth(200); // 时间

        // 修复表格渲染器问题
        scoreList.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // 设置选择颜色
        scoreList.setSelectionBackground(SELECTION_COLOR);
        scoreList.setSelectionForeground(TEXT_COLOR);

        // 美化滚动面板
        scoreScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scoreScroll.getViewport().setBackground(Color.WHITE);
    }

    // 自定义表格单元格渲染器 - 修复渲染问题
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 设置居中对齐
            setHorizontalAlignment(SwingConstants.CENTER);

            // 处理选中状态
            if (isSelected) {
                c.setBackground(SELECTION_COLOR);
                c.setForeground(TEXT_COLOR);
            } else {
                // 设置交替行颜色
                if (row % 2 == 0) {
                    c.setBackground(TABLE_ROW_COLOR1);
                } else {
                    c.setBackground(TABLE_ROW_COLOR2);
                }
                c.setForeground(TEXT_COLOR);
            }

            // 确保文本始终可见
            if (value != null) {
                setText(value.toString());
            } else {
                setText("");
            }

            return c;
        }
    }

    private void beautifyButton(JButton button, Color baseColor) {
        if (button == null) return;

        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // 设置按钮大小
        button.setPreferredSize(new Dimension(120, 40));
        button.setMinimumSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));

        // 添加圆角效果
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

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
    }

    private String getDifficultyName(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "简单模式";
            case NORMAL: return "普通模式";
            case HARD: return "困难模式";
            default: return "未知模式";
        }
    }

    private void loadRecords() {
        tableModel.setRowCount(0); // 清空现有数据

        // 加载对应难度的记录文件
        String filePath = getFilePathForDifficulty(difficulty);
        playerDao.loadFromFile(filePath);

        // 填充表格
        int rank = 1;
        for (Player player : playerDao.getAllPlayers()) {
            tableModel.addRow(new Object[]{
                    rank,
                    player.getPlayerName(),
                    player.getScore(),
                    player.getPlayTime()
            });
            rank++;
        }

        // 强制刷新表格
        tableModel.fireTableDataChanged();

        // 清除选择
        scoreList.clearSelection();

        // 如果没有记录，显示提示信息
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(MainPanel,
                    "暂无游戏记录！\n快去开始一局游戏吧！",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String getFilePathForDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "ranking_easy.txt";
            case NORMAL: return "ranking_normal.txt";
            case HARD: return "ranking_hard.txt";
            default: return "ranking.txt";
        }
    }

    private void setupListeners() {
        // 删除按钮事件
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = scoreList.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(MainPanel,
                            "请先选择要删除的记录！",
                            "提示",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 获取选中的玩家信息用于确认
                String playerName = (String) tableModel.getValueAt(selectedRow, 1);
                int score = (int) tableModel.getValueAt(selectedRow, 2);

                int confirm = JOptionPane.showConfirmDialog(
                        MainPanel,
                        "确定要删除玩家 '" + playerName + "' 的记录吗？\n得分: " + score,
                        "确认删除",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    // 从数据源删除
                    playerDao.deletePlayerByIndex(selectedRow);

                    // 重新加载并刷新
                    loadRecords();

                    // 显示删除成功提示
                    JOptionPane.showMessageDialog(MainPanel,
                            "记录删除成功！",
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // 返回按钮事件
        returnBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 保存当前记录
                String filePath = getFilePathForDifficulty(difficulty);
                playerDao.saveToFile(filePath);

                // 切换到主菜单
                Main.cardLayout.show(Main.cardPanel, "menu");

                // 给一点延迟确保UI更新完成
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        // 强制垃圾回收，清理残留对象
                        System.gc();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }
}