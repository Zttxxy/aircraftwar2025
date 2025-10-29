// EasyGame.java - 实现简单难度
package edu.hitsz.application;
import java.awt.image.BufferedImage;

public class EasyGame extends Game {

    public EasyGame() {
        super(ImageManager.BACKGROUND_IMAGE_EASY);
        setDifficulty(Difficulty.EASY);
    }

    @Override
    protected void initializeDifficulty() {
        // 简单模式配置
        this.difficulty = Difficulty.EASY;
        this.enemyMaxNumber = 3;
        this.cycleDuration = 600;
        this.bossThreshold = Integer.MAX_VALUE; // 简单模式没有Boss
        this.mobEnemyHp = 30;
        this.eliteEnemyHp = 50;
        this.elitePlusHp = 80;
        this.bossEnemyHp = 0;
        this.eliteEnemyProbability = 0.15;  // 15% 精英敌机
        this.elitePlusProbability = 0.05;   // 5% 超级精英敌机
        this.enemySpeedBonus = 0;

        System.out.println("初始化简单模式：无Boss敌机，敌机数量较少");
    }

    @Override
    protected void increaseDifficulty() {
        // 简单模式不随时间增加难度
        // 空实现
    }

    @Override
    protected void generateEnemies() {
        // 简单模式没有Boss敌机，只生成普通敌机
        while (enemyAircrafts.size() < enemyMaxNumber) {
            addRandomEnemy();
        }
    }
}