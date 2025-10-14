package edu.hitsz.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DaoPatternDemo {
    public static void main(String[] args) {
        PlayerDaoImpl playerDao = new PlayerDaoImpl();

        // 模拟游戏结束后记录玩家得分
        playerDao.addPlayer(new Player("Alice", 500, PlayerDaoImpl.currentTime()));
        playerDao.addPlayer(new Player("Bob", 800, PlayerDaoImpl.currentTime()));
        playerDao.addPlayer(new Player("Charlie", 300, PlayerDaoImpl.currentTime()));

        // 输出排行榜
        playerDao.sortPlayersByScore();
        playerDao.printRanking();
    }

    private static String currentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
