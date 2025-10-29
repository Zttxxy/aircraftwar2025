// CommonGame.java - 实现普通难度
package edu.hitsz.application;
import java.awt.image.BufferedImage;

public class CommonGame extends Game {
    private int difficultyIncreaseCount = 0;

    public CommonGame() {
        super(ImageManager.BACKGROUND_IMAGE_COMMON);
        setDifficulty(Difficulty.NORMAL);
    }

    @Override
    protected void initializeDifficulty() {
        // 普通模式配置
        this.difficulty = Difficulty.NORMAL;
        this.enemyMaxNumber = 4;
        this.cycleDuration = 500;
        this.bossThreshold = 200;
        this.mobEnemyHp = 40;
        this.eliteEnemyHp = 70;
        this.elitePlusHp = 120;
        this.bossEnemyHp = 500;
        this.eliteEnemyProbability = 0.25;  // 25% 精英敌机
        this.elitePlusProbability = 0.10;   // 10% 超级精英敌机
        this.enemySpeedBonus = 0;

        System.out.println("初始化普通模式：有Boss敌机，中等难度");
    }

    @Override
    protected void increaseDifficulty() {
        difficultyIncreaseCount++;

        // 每10秒提升一次难度
        if (difficultyIncreaseCount <= 5) { // 最多提升5次
            enemyMaxNumber = Math.min(6, enemyMaxNumber + 1);
            cycleDuration = Math.max(300, cycleDuration - 40);
            mobEnemyHp += 5;
            eliteEnemyHp += 8;
            elitePlusHp += 10;
            enemySpeedBonus += 1;

            System.out.println("普通模式难度提升！等级：" + difficultyIncreaseCount);
            System.out.println("敌机最大数量：" + enemyMaxNumber +
                    ", 生成周期：" + cycleDuration +
                    ", 敌机速度加成：" + enemySpeedBonus);
        }
    }

    @Override
    protected int getBossEnemyHp() {
        // 普通模式Boss血量固定
        return bossEnemyHp;
    }
}