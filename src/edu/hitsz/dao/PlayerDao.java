package edu.hitsz.dao;

import java.util.List;

public interface PlayerDao {
    List<Player> getAllPlayers();
    void addPlayer(Player player);      // 添加玩家
    void deletePlayer(String playerName);
    void sortPlayersByScore();
    void printRanking();
    void saveToFile(String filePath);
}

