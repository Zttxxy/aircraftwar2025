package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.factory.*;
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
 * 游戏主面板
 */
public class Game extends JPanel {

    protected final List<AbstractProp> props;
    protected final ScheduledExecutorService executorService;
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
    private MusicThread bgmThread;
    private MusicThread bossBgmThread;
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

    // 修改构造函数
    public Game(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
        // 默认难度为普通
        this.difficulty = Difficulty.NORMAL;

        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 1000);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        new HeroController(this);
    }

    // 设置难度的方法
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void action() {
        Runnable task = () -> {
            time += timeInterval;

            // 敌机生成与射击
            if (timeCountAndNewCycleJudge()) {
                boolean bossAlive = enemyAircrafts.stream().anyMatch(e -> e instanceof BossEnemy);
                if (score >= nextBossScore && !bossAlive) {
                    EnemyFactory bossFactory = new BossEnemyFactory();
                    AbstractAircraft boss = bossFactory.createEnemy(
                            Main.WINDOW_WIDTH / 2,
                            (int) (Main.WINDOW_HEIGHT * 0.05),
                            2, 0, 500
                    );
                    enemyAircrafts.add(boss);
                    System.out.println("Boss 出现！");

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

                while (enemyAircrafts.size() < enemyMaxNumber) {
                    double rand = Math.random();
                    if (rand < 0.6) addRandomEnemy(new MobEnemyFactory());
                    else if (rand < 0.85) addRandomEnemy(new EliteEnemyFactory());
                    else addRandomEnemy(new ElitePlusFactory());
                }

                shootAction();
            }

            bulletsMoveAction();
            aircraftsMoveAction();
            propsMoveAction();
            crashCheckAction();
            postProcessAction();
            repaint();

            if (heroAircraft.getHp() <= 0) {
                executorService.shutdown();
                heroAircraft.cleanup();
                gameOverFlag = true;
                System.out.println("Game Over!");

                stopAllMusic();

                // 🎵 播放游戏结束音效（使用新的MusicThread）
                if (ifMusicOn) {
                    MusicThread gameOverSound = new MusicThread(SFX_GAMEOVER, false, true);
                    gameOverSound.start();
                }

                // 保存玩家信息
                String playerName = JOptionPane.showInputDialog(this, "游戏结束，输入你的名字以记录成绩：");
                if (playerName != null && !playerName.trim().isEmpty()) {
                    PlayerDao playerDao = new PlayerDaoImpl();
                    String filePath = getRankingFilePath(difficulty);
                    playerDao.loadFromFile(filePath); // 先加载现有记录
                    playerDao.addPlayer(new Player(playerName.trim(), score, PlayerDaoImpl.currentTime()));
                }

                // 切换到该难度的成绩表
                SwingUtilities.invokeLater(() -> {
                    // 移除旧的score面板（如果存在）
                    Component[] components = Main.cardPanel.getComponents();
                    for (Component c : components) {
                        if ("score".equals(c.getName())) {
                            Main.cardPanel.remove(c);
                            break;
                        }
                    }

                    // 创建新的排行榜
                    ScoreTable scoreTable = new ScoreTable(this.difficulty);
                    scoreTable.getMainPanel().setName("score");
                    Main.cardPanel.add(scoreTable.getMainPanel(), "score");

                    // 显示排行榜
                    Main.cardLayout.show(Main.cardPanel, "score");
                });
            }
        };

        // 🎵 开始普通背景音乐
        if (ifMusicOn && bgmThread == null) {
            System.out.println("初始化背景音乐，ifMusicOn=" + ifMusicOn);
            bgmThread = new MusicThread(BGM_PATH, true, true);
            bgmThread.start();

            // 添加状态检查
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // 等待2秒
                    if (bgmThread != null && bgmThread.isAlive()) {
                        System.out.println("背景音乐线程仍在运行");
                    } else {
                        System.out.println("背景音乐线程已停止");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }

        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    // 添加获取排行榜文件路径的方法
    private String getRankingFilePath(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "ranking_easy.txt";
            case NORMAL: return "ranking_normal.txt";
            case HARD: return "ranking_hard.txt";
            default: return "ranking.txt";
        }
    }

    protected void addRandomEnemy(EnemyFactory factory) {
        int x = (int) (Math.random() * (Main.WINDOW_WIDTH - 50));
        int y = (int) (Math.random() * 50);
        int speedY = 3 + (int) (Math.random() * 3);
        enemyAircrafts.add(factory.createEnemy(x, y, 0, speedY, 50));
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
                prop.active(heroAircraft);
                prop.vanish();

                // 🎵 道具音效
                if (ifMusicOn) new MusicThread(SFX_PROP, false, true).start();

                // 特殊处理火力道具
                if (prop instanceof FireProp) {
                    // FireProp：散射策略，持续5秒
                    heroAircraft.setTemporaryStrategy(new ScatterFireStrategy(), 5000);
                    System.out.println("切换到散射模式，持续5秒");
                } else if (prop instanceof SuperFireProp) {
                    // SuperFireProp：环形散射策略，持续8秒
                    heroAircraft.setTemporaryStrategy(new SuperFireStrategy(), 8000);
                    System.out.println("切换到环形散射模式，持续8秒");
                } else if (prop instanceof BombProp && ifMusicOn) {
                    new MusicThread(SFX_EXPLOSION, false, true).start();
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