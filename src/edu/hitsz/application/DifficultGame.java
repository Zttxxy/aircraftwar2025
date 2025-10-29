// DifficultGame.java - 实现困难难度
package edu.hitsz.application;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.factory.BossEnemyFactory;
import edu.hitsz.factory.EnemyFactory;

import java.awt.image.BufferedImage;

public class DifficultGame extends Game {
    private int difficultyIncreaseCount = 0;
    private int bossAppearanceCount = 0;

    public DifficultGame() {
        super(ImageManager.BACKGROUND_IMAGE_DIFFICULT);
        setDifficulty(Difficulty.HARD);
    }

    @Override
    protected void initializeDifficulty() {
        // 困难模式配置
        this.difficulty = Difficulty.HARD;
        this.enemyMaxNumber = 5;
        this.cycleDuration = 400;
        this.bossThreshold = 150; // 更低的Boss出现阈值
        this.mobEnemyHp = 50;
        this.eliteEnemyHp = 90;
        this.elitePlusHp = 150;
        this.bossEnemyHp = 600;
        this.eliteEnemyProbability = 0.30;  // 30% 精英敌机
        this.elitePlusProbability = 0.15;   // 15% 超级精英敌机
        this.enemySpeedBonus = 1;

        System.out.println("初始化困难模式：高难度，Boss出现更频繁且更强");
    }

    @Override
    protected void increaseDifficulty() {
        difficultyIncreaseCount++;

        // 每10秒提升一次难度，提升幅度更大
        if (difficultyIncreaseCount <= 8) { // 最多提升8次
            enemyMaxNumber = Math.min(8, enemyMaxNumber + 1);
            cycleDuration = Math.max(200, cycleDuration - 25);
            mobEnemyHp += 8;
            eliteEnemyHp += 12;
            elitePlusHp += 15;
            enemySpeedBonus += 1;
            eliteEnemyProbability = Math.min(0.40, eliteEnemyProbability + 0.02);
            elitePlusProbability = Math.min(0.20, elitePlusProbability + 0.01);

            System.out.println("困难模式难度大幅提升！等级：" + difficultyIncreaseCount);
            System.out.println("敌机最大数量：" + enemyMaxNumber +
                    ", 生成周期：" + cycleDuration +
                    ", 精英概率：" + String.format("%.2f", eliteEnemyProbability));
        }
    }

    @Override
    protected int getBossEnemyHp() {
        // 困难模式每次Boss出现血量增加
        bossAppearanceCount++;
        int additionalHp = (bossAppearanceCount - 1) * 100; // 每次增加100血量
        return bossEnemyHp + additionalHp;
    }

    @Override
    protected void generateBossEnemy() {
        EnemyFactory bossFactory = new BossEnemyFactory();
        int currentBossHp = getBossEnemyHp();
        AbstractAircraft boss = bossFactory.createEnemy(
                Main.WINDOW_WIDTH / 2,
                (int) (Main.WINDOW_HEIGHT * 0.05),
                2, 0, currentBossHp
        );
        enemyAircrafts.add(boss);
        System.out.println("Boss 第 " + bossAppearanceCount + " 次出现！血量：" + currentBossHp);

        // 🎵 切换为Boss音乐
        if (ifMusicOn) {
            if (bgmThread != null) {
                bgmThread.setMusicFlag(false);
                bgmThread = null;
            }
            bossBgmThread = new MusicThread("src/videos/bgm_boss.wav", true, true);
            bossBgmThread.start();
        }

        nextBossScore += bossThreshold;
    }
}