package edu.hitsz.application;

import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    public ScoreTable(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.playerDao = new PlayerDaoImpl();

        initializeUI();
        loadRecords();
        setupListeners();
    }

    private void initializeUI() {
        // 设置难度标签
        mode.setText("难度: " + getDifficultyName(difficulty));

        // 初始化表格
        String[] columnNames = {"排名", "玩家名称", "得分", "时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        scoreList.setModel(tableModel);

        // 设置表格样式
        scoreList.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        scoreList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        scoreList.setRowHeight(25);
        scoreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置列宽
        scoreList.getColumnModel().getColumn(0).setPreferredWidth(60);  // 排名
        scoreList.getColumnModel().getColumn(1).setPreferredWidth(100); // 玩家名称
        scoreList.getColumnModel().getColumn(2).setPreferredWidth(80);  // 得分
        scoreList.getColumnModel().getColumn(3).setPreferredWidth(150); // 时间
    }

    private String getDifficultyName(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "简单";
            case NORMAL: return "普通";
            case HARD: return "困难";
            default: return "未知";
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
                    JOptionPane.showMessageDialog(MainPanel, "请先选择要删除的记录！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        MainPanel,
                        "确定要删除这条记录吗？",
                        "确认删除",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    // 从数据源删除
                    playerDao.deletePlayerByIndex(selectedRow);
                    loadRecords(); // 重新加载
                }
            }
        });

        // 返回按钮事件
        returnBotton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 切换到主菜单
                Main.cardLayout.show(Main.cardPanel, "menu");
            }
        });
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }
}