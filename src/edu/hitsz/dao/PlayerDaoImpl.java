package edu.hitsz.dao;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PlayerDaoImpl implements PlayerDao {
    private List<Player> players = new ArrayList<>();
    private String currentFilePath;

    @Override
    public List<Player> getAllPlayers() {
        return players;
    }

    @Override
    public void addPlayer(Player player) {
        players.add(player);
        // 添加后自动排序
        sortPlayersByScore();
        // 自动保存到当前文件
        if (currentFilePath != null) {
            saveToFile(currentFilePath);
        }
    }

    @Override
    public void deletePlayer(String playerName) {
        players.removeIf(p -> p.getPlayerName().equals(playerName));
        if (currentFilePath != null) {
            saveToFile(currentFilePath);
        }
    }

    @Override
    public void deletePlayerByIndex(int index) {
        if (index >= 0 && index < players.size()) {
            players.remove(index);
            if (currentFilePath != null) {
                saveToFile(currentFilePath);
            }
        }
    }

    @Override
    public void sortPlayersByScore() {
        players.sort((p1, p2) -> p2.getScore() - p1.getScore());
    }

    @Override
    public void printRanking() {
        int rank = 1;
        System.out.println("排行榜:");
        for (Player p : players) {
            System.out.println(rank + ". " + p.getPlayerName() + " | 分数: " + p.getScore() + " | 时间: " + p.getPlayTime());
            rank++;
        }
    }

    @Override
    public void saveToFile(String filePath) {
        this.currentFilePath = filePath;
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Player player : players) {
                writer.println(player.getPlayerName() + "," + player.getScore() + "," + player.getPlayTime());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromFile(String filePath) {
        this.currentFilePath = filePath;
        players.clear();

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String playerName = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    String playTime = parts[2];
                    players.add(new Player(playerName, score, playTime));
                }
            }
            // 加载后排序
            sortPlayersByScore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String currentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}