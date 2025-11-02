package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.factory.*;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.SuperFireProp;
import edu.hitsz.strategy.ScatterFireStrategy;
import edu.hitsz.strategy.SuperFireStrategy;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板 - 抽象模板类
 */
public abstract class Game extends JPanel {

    protected final List<AbstractProp> props;
    protected ScheduledExecutorService executorService;
    protected final HeroAircraft heroAircraft;
    protected final List<AbstractAircraft> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;

    protected int backGroundTop = 0;
    protected int timeInterval = 40;
    protected int enemyMaxNumber = 3;
    protected int score = 0;
    protected int time = 0;
    protected int cycleDuration = 600;
    protected int cycleTime = 0;
    protected boolean gameOverFlag = false;

    protected int bossThreshold = 200;
    protected int nextBossScore = bossThreshold;
    protected boolean bossExists = false;

    protected BufferedImage backgroundImage;

    // ---------- 🎵 音乐控制 ----------
    protected MusicThread bgmThread;
    protected MusicThread bossBgmThread;
    public boolean ifMusicOn = false;  // 音乐开关

    // 音效文件路径常量
    private static final String BGM_PATH = "src/videos/bgm.wav";
    private static final String BOSS_BGM_PATH = "src/videos/bgm_boss.wav";
    private static final String SFX_HIT = "src/videos/bullet_hit.wav";
    private static final String SFX_EXPLOSION = "src/videos/bomb_explosion.wav";
    private static final String SFX_PROP = "src/videos/get_supply.wav";
    private static final String SFX_GAMEOVER = "src/videos/game_over.wav";

    // 添加难度字段
    protected Difficulty difficulty;

    // 模板模式相关字段 - 难度参数
    protected int mobEnemyHp = 30;
    protected int eliteEnemyHp = 60;
    protected int elitePlusHp = 100;
    protected int bossEnemyHp = 500;
    protected double eliteEnemyProbability = 0.25;
    protected double elitePlusProbability = 0.15;
    protected int enemySpeedBonus = 0;
    protected int cycleTimeReduction = 0;

    public Game(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;

        // 调用模板方法进行难度初始化
        initializeDifficulty();

        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 5000);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        new HeroController(this);
    }

    /**
     * 完全停止游戏并清理所有资源
     */
    public void stopGame() {
        // 设置游戏结束标志
        gameOverFlag = true;

        // 停止游戏循环
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        System.err.println("线程池没有正常终止");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 停止所有音乐
        stopAllMusic();

        // 清理英雄机的定时任务
        if (heroAircraft != null) {
            heroAircraft.cleanup();
        }

        System.out.println("游戏已完全停止");
    }

    /**
     * 重置游戏状态
     */
    protected void resetGameState() {
        // 先停止当前游戏
        stopGame();

        // 重置游戏变量
        this.score = 0;
        this.time = 0;
        this.cycleTime = 0;
        this.gameOverFlag = false;
        this.nextBossScore = bossThreshold;
        this.bossExists = false;

        // 清空所有列表
        this.enemyAircrafts.clear();
        this.heroBullets.clear();
        this.enemyBullets.clear();
        this.props.clear();

        // 重置背景位置
        this.backGroundTop = 0;

        // 重置英雄机位置和状态
        if (this.heroAircraft != null) {
            this.heroAircraft.reset();
            this.heroAircraft.setLocation(Main.WINDOW_WIDTH / 2,
                    Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight());
        }

        // 重新创建线程池（只有在需要时才创建）
        if (executorService == null || executorService.isShutdown()) {
            this.executorService = new ScheduledThreadPoolExecutor(1,
                    new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());
        }
    }

    /**
     * 模板方法：初始化游戏难度设置
     * 子类必须实现此方法来配置难度参数
     */
    protected abstract void initializeDifficulty();

    /**
     * 模板方法：随时间增加难度
     * 子类可以实现此方法来动态调整难度
     */
    protected void increaseDifficulty() {
        // 默认实现：简单模式不增加难度
        System.out.println("当前难度：" + difficulty + " - 难度未提升");
    }

    /**
     * 模板方法：获取Boss敌机血量
     */
    protected int getBossEnemyHp() {
        return bossEnemyHp;
    }

    /**
     * 模板方法：敌机生成逻辑
     */
    protected void generateEnemies() {
        boolean bossAlive = enemyAircrafts.stream().anyMatch(e -> e instanceof BossEnemy);
        if (score >= nextBossScore && !bossAlive) {
            generateBossEnemy();
        }

        while (enemyAircrafts.size() < enemyMaxNumber) {
            addRandomEnemy();
        }
    }

    /**
     * 具体方法：生成Boss敌机
     */
    protected void generateBossEnemy() {
        EnemyFactory bossFactory = new BossEnemyFactory();
        AbstractAircraft boss = bossFactory.createEnemy(
                Main.WINDOW_WIDTH / 2,
                (int) (Main.WINDOW_HEIGHT * 0.05),
                2, 0, getBossEnemyHp()
        );
        enemyAircrafts.add(boss);
        System.out.println("Boss 出现！血量：" + getBossEnemyHp());

        // 🎵 切换为Boss音乐
        if (ifMusicOn) {
            if (bgmThread != null) {
                bgmThread.setMusicFlag(false);
                bgmThread = null;
            }
            bossBgmThread = new MusicThread(BOSS_BGM_PATH, true, true);
            bossBgmThread.start();
        }

        nextBossScore += bossThreshold;
    }

    /**
     * 具体方法：添加随机敌机
     */
    protected void addRandomEnemy() {
        double rand = Math.random();
        int x = (int) (Math.random() * (Main.WINDOW_WIDTH - 50));
        int y = (int) (Math.random() * 50);
        int speedY = 3 + (int) (Math.random() * 3) + enemySpeedBonus;

        if (rand < (1 - eliteEnemyProbability - elitePlusProbability)) {
            // 生成普通敌机
            EnemyFactory mobFactory = new MobEnemyFactory();
            enemyAircrafts.add(mobFactory.createEnemy(x, y, 0, speedY, mobEnemyHp));
        } else if (rand < (1 - elitePlusProbability)) {
            // 生成精英敌机
            EnemyFactory eliteFactory = new EliteEnemyFactory();
            enemyAircrafts.add(eliteFactory.createEnemy(x, y, 0, speedY, eliteEnemyHp));
        } else {
            // 生成超级精英敌机
            EnemyFactory elitePlusFactory = new ElitePlusFactory();
            enemyAircrafts.add(elitePlusFactory.createEnemy(x, y, 0, speedY, elitePlusHp));
        }
    }

    /**
     * 具体方法：游戏主循环
     */
    public void action() {
        // 确保游戏状态已重置
        resetGameState();

        Runnable gameLoopTask = () -> {
            // 检查游戏是否应该继续
            if (gameOverFlag || executorService.isShutdown()) {
                return;
            }

            time += timeInterval;

            // 敌机生成与射击
            if (timeCountAndNewCycleJudge()) {
                generateEnemies();
                shootAction();

                // 每10秒增加一次难度
                if (time % 10000 == 0) {
                    increaseDifficulty();
                }
            }

            bulletsMoveAction();
            aircraftsMoveAction();
            propsMoveAction();
            crashCheckAction();
            postProcessAction();
            repaint();

            if (heroAircraft.getHp() <= 0) {
                handleGameOver();
            }
        };

        // 🎵 开始普通背景音乐
        if (ifMusicOn && bgmThread == null) {
            System.out.println("初始化背景音乐，ifMusicOn=" + ifMusicOn);
            bgmThread = new MusicThread(BGM_PATH, true, true);
            bgmThread.start();
        }

        // 确保线程池可用
        if (executorService == null || executorService.isShutdown()) {
            this.executorService = new ScheduledThreadPoolExecutor(1,
                    new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());
        }

        executorService.scheduleWithFixedDelay(gameLoopTask, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 具体方法：游戏结束处理
     */
    protected void handleGameOver() {
        executorService.shutdown();
        heroAircraft.cleanup();
        gameOverFlag = true;
        System.out.println("Game Over!");

        stopAllMusic();

        // 🎵 播放游戏结束音效
        if (ifMusicOn) {
            MusicThread gameOverSound = new MusicThread(SFX_GAMEOVER, false, true);
            gameOverSound.start();
        }

        // 保存玩家信息
        String playerName = JOptionPane.showInputDialog(this, "游戏结束，输入你的名字以记录成绩：");
        if (playerName != null && !playerName.trim().isEmpty()) {
            PlayerDao playerDao = new PlayerDaoImpl();
            String filePath = getRankingFilePath(difficulty);
            playerDao.loadFromFile(filePath);
            playerDao.addPlayer(new Player(playerName.trim(), score, PlayerDaoImpl.currentTime()));
        }

        // 切换到该难度的成绩表
        SwingUtilities.invokeLater(() -> {
            Component[] components = Main.cardPanel.getComponents();
            for (Component c : components) {
                if ("score".equals(c.getName())) {
                    Main.cardPanel.remove(c);
                    break;
                }
            }

            ScoreTable scoreTable = new ScoreTable(this.difficulty);
            scoreTable.getMainPanel().setName("score");
            Main.cardPanel.add(scoreTable.getMainPanel(), "score");
            Main.cardLayout.show(Main.cardPanel, "score");
        });
    }

    public void setBgmThread(MusicThread bgmThread) {
        this.bgmThread = bgmThread;
    }

    public void setBossBgmThread(MusicThread bossBgmThread) {
        this.bossBgmThread = bossBgmThread;
    }

    public void stopAllMusic() {
        if (bgmThread != null) {
            bgmThread.setMusicFlag(false);
            bgmThread = null;
        }
        if (bossBgmThread != null) {
            bossBgmThread.setMusicFlag(false);
            bossBgmThread = null;
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    private String getRankingFilePath(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "ranking_easy.txt";
            case NORMAL: return "ranking_normal.txt";
            case HARD: return "ranking_hard.txt";
            default: return "ranking.txt";
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        }
        return false;
    }

    private void shootAction() {
        heroBullets.addAll(heroAircraft.shoot());
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (!enemy.notValid()) enemyBullets.addAll(enemy.shoot());
        }
    }

    private void bulletsMoveAction() {
        heroBullets.forEach(BaseBullet::forward);
        enemyBullets.forEach(BaseBullet::forward);
    }

    private void aircraftsMoveAction() {
        enemyAircrafts.forEach(AbstractAircraft::forward);
    }

    private void propsMoveAction() {
        props.forEach(AbstractProp::forward);
    }

    private void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) continue;
                if (enemy.crash(bullet)) {
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();

                    if (enemy.notValid()) {
                        score += 10;

                        // 🎵 敌机被击毁音效
                        if (ifMusicOn) new MusicThread(SFX_HIT, false, true).start();

                        // 道具掉落逻辑
                        int maxDrop = 0;
                        if (enemy instanceof BossEnemy) {
                            // Boss被击败，切换回普通音乐
                            if (ifMusicOn) {
                                if (bossBgmThread != null) {
                                    bossBgmThread.setMusicFlag(false);
                                    bossBgmThread = null;
                                }
                                bgmThread = new MusicThread(BGM_PATH, true, true);
                                bgmThread.start();
                            }
                        }
                        else if (enemy instanceof EliteEnemy || enemy instanceof ElitePlus) maxDrop = 1;

                        for (int i = 0; i < (int) (Math.random() * (maxDrop + 1)); i++) {
                            double p = Math.random();
                            PropFactory f;
                            if (p < 0.3) f = new BloodPropFactory();
                            else if (p < 0.6) f = new BombPropFactory();
                            else if (p < 0.85) f = new FirePropFactory();
                            else f = new SuperFirePropFactory();
                            props.add(f.createProp(enemy.getLocationX(), enemy.getLocationY(), 0, 5));
                        }
                    }
                }

                // 英雄与敌机碰撞
                if (enemy.crash(heroAircraft)) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(50);
                }
            }
        }

        // 英雄拾取道具
        for (AbstractProp prop : props) {
            if (!prop.notValid() && heroAircraft.crash(prop)) {
                // 检查游戏是否还在运行
                if (gameOverFlag || executorService.isShutdown()) {
                    continue;
                }

                // 如果是炸弹道具，先注册所有观察者
                if (prop instanceof BombProp) {
                    BombProp bombProp = (BombProp) prop;
                    // 注册所有敌机为观察者
                    for (AbstractAircraft enemy : enemyAircrafts) {
                        if (enemy instanceof BombObserver && !enemy.notValid()) {
                            bombProp.attachObserver((BombObserver) enemy);
                        }
                    }
                    // 注册所有敌机子弹为观察者
                    for (BaseBullet bullet : enemyBullets) {
                        if (bullet instanceof BombObserver && !bullet.notValid()) {
                            bombProp.attachObserver((BombObserver) bullet);
                        }
                    }
                }

                prop.active(heroAircraft);
                prop.vanish();

                // 🎵 道具音效
                if (ifMusicOn) {
                    MusicThread propSound = new MusicThread(SFX_PROP, false, true);
                    propSound.start();
                }

                // 特殊处理火力道具 - 添加更严格的状态检查
                if (prop instanceof FireProp) {
                    // 双重检查游戏状态
                    if (!gameOverFlag && !executorService.isShutdown() && heroAircraft != null) {
                        try {
                            heroAircraft.setTemporaryStrategy(new ScatterFireStrategy(), 8000);
                            System.out.println("切换到散射模式，持续8秒");
                        } catch (Exception e) {
                            System.err.println("设置散射模式时出错: " + e.getMessage());
                        }
                    }
                } else if (prop instanceof SuperFireProp) {
                    if (!gameOverFlag && !executorService.isShutdown() && heroAircraft != null) {
                        try {
                            heroAircraft.setTemporaryStrategy(new SuperFireStrategy(), 8000);
                            System.out.println("切换到环形散射模式，持续8秒");
                        } catch (Exception e) {
                            System.err.println("设置环形散射模式时出错: " + e.getMessage());
                        }
                    }
                } else if (prop instanceof BombProp && ifMusicOn) {
                    MusicThread explosionSound = new MusicThread(SFX_EXPLOSION, false, true);
                    explosionSound.start();
                }
            }
        }
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 1. 绘制背景
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, backGroundTop - Main.WINDOW_HEIGHT, null);
            g.drawImage(backgroundImage, 0, backGroundTop, null);
            backGroundTop += 1;
            if (backGroundTop == Main.WINDOW_HEIGHT) backGroundTop = 0;
        } else {
            // 如果背景图片为空，绘制黑色背景
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. 绘制游戏对象
        paintObjects(g, enemyBullets);
        paintObjects(g, heroBullets);
        paintObjects(g, enemyAircrafts);
        paintObjects(g, props);

        // 3. 绘制英雄机
        if (ImageManager.HERO_IMAGE != null) {
            g.drawImage(ImageManager.HERO_IMAGE,
                    heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                    heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);
        }

        // 4. 绘制分数和生命值
        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE: " + score, 10, 25);
        g.drawString("LIFE: " + heroAircraft.getHp(), 10, 50);

        // 5. 绘制射击模式状态
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));

        String strategyName = heroAircraft.getCurrentStrategyName();
        if (!strategyName.equals("NormalFireStrategy")) {
            String displayName = "";
            switch (strategyName) {
                case "ScatterFireStrategy": displayName = "散射模式"; break;
                case "FanFireStrategy": displayName = "扇形散射"; break;
                case "SuperFireStrategy": displayName = "环形散射"; break;
                default: displayName = strategyName;
            }
            g.drawString("模式: " + displayName, 10, 75);
        }
    }

    private void paintObjects(Graphics g, List<? extends AbstractFlyingObject> list) {
        for (AbstractFlyingObject o : list) {
            g.drawImage(o.getImage(),
                    o.getLocationX() - o.getImage().getWidth() / 2,
                    o.getLocationY() - o.getImage().getHeight() / 2, null);
        }
    }
}