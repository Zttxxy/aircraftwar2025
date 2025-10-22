package edu.hitsz.dao;

import java.util.List;

public interface PlayerDao {
    List<Player> getAllPlayers();
    void addPlayer(Player player);
    void deletePlayer(String playerName);
    void deletePlayerByIndex(int index); // 新增：按索引删除
    void sortPlayersByScore();
    void printRanking();
    void saveToFile(String filePath);
    void loadFromFile(String filePath); // 新增：从文件加载
}