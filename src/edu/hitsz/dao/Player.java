package edu.hitsz.dao;

public class Player {
    private String playerName;
    private int score;
    private String playTime;

    public Player(String playerName, int score, String playTime) {
        this.playerName = playerName;
        this.score = score;
        this.playTime = playTime;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public String getPlayTime() { return playTime; }

    @Override
    public String toString() {
        return playerName + " | " + score + " | " + playTime;
    }
}
