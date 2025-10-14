package edu.hitsz.dao;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PlayerDaoImpl implements PlayerDao {
    private List<Player> players = new ArrayList<>();

    @Override
    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public List<Player> getAllPlayers() {
        return players;
    }

    @Override
    public void deletePlayer(String playerName) {
        players.removeIf(p -> p.getPlayerName().equals(playerName));
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
        // 写入文件的逻辑，可选
    }

    public static String currentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
