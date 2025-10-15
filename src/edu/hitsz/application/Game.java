package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板（基础类，不同模式继承）
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

    protected BufferedImage backgroundImage; // ✅ 新增背景图片属性

    // ---------- 音乐控制 ----------
    private MusicThread bgmThread;          // 背景音乐线程
    private MusicThread bossBgmThread;      // Boss 背景音乐线程
    // Game.java 中新增
    public boolean ifMusicOn = false;


    public void setBgmThread(MusicThread bgmThread) {
        this.bgmThread = bgmThread;
    }

    public void setBossBgmThread(MusicThread bossBgmThread) {
        this.bossBgmThread = bossBgmThread;
    }

    /**
     * 停止所有正在播放的背景音乐
     */
    public void stopAllMusic() {
        if (bgmThread != null) {
            bgmThread.stopMusic();
            bgmThread = null;
        }
        if (bossBgmThread != null) {
            bossBgmThread.stopMusic();
            bossBgmThread = null;
        }
    }



    public Game(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;

        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 1000000);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        new HeroController(this);
    }

    public void action() {
        Runnable task = () -> {
            time += timeInterval;

            // 周期性事件（敌机生成 + 射击）
            if (timeCountAndNewCycleJudge()) {
                boolean bossExists = enemyAircrafts.stream().anyMatch(e -> e instanceof BossEnemy);

                // Boss 出场逻辑
                if (score >= nextBossScore && !bossExists) {
                    EnemyFactory bossFactory = new BossEnemyFactory();
                    AbstractAircraft boss = bossFactory.createEnemy(
                            Main.WINDOW_WIDTH / 2,
                            (int) (Main.WINDOW_HEIGHT * 0.05),
                            2, 0, 500
                    );
                    enemyAircrafts.add(boss);
                    System.out.println("Boss 出现！");

                    // 播放 Boss 背景音乐
                    if (ifMusicOn) {
                        if (bossBgmThread != null) bossBgmThread.stopMusic();
                        bossBgmThread = new MusicThread("src/videos/boss_bgm.wav");
                        bossBgmThread.setLoop(true);
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
                gameOverFlag = true;
                System.out.println("Game Over!");

                // 游戏结束停止音乐
                stopAllMusic();

                // 保存玩家信息
                PlayerDaoImpl dao = new PlayerDaoImpl();
                dao.addPlayer(new Player("Player1", score, PlayerDaoImpl.currentTime()));
                dao.sortPlayersByScore();
                dao.printRanking();
                dao.saveToFile("ranking.txt");

                // 播放游戏结束音效
                if (ifMusicOn) {
                    MusicThread gameOverSound = new MusicThread("src/videos/game_over.wav");
                    gameOverSound.start();
                }
            }
        };

        // 游戏开始时播放普通背景音乐
        if (ifMusicOn && bgmThread == null) {
            bgmThread = new MusicThread("src/videos/bgm.wav");
            bgmThread.setLoop(true);
            bgmThread.start();
        }

        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }


    protected void addRandomEnemy(EnemyFactory factory) {
        int enemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
        if (factory instanceof EliteEnemyFactory || factory instanceof ElitePlusFactory)
            enemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();

        int x = (int) (Math.random() * (Main.WINDOW_WIDTH - enemyWidth));
        int y = 20 + (int) (Math.random() * 50);
        int speedX = (int) (Math.random() * 3) - 1;
        int speedY = 3 + (int) (Math.random() * 3);

        enemyAircrafts.add(factory.createEnemy(x, y, speedX, speedY, factory instanceof MobEnemyFactory ? 30 :
                factory instanceof EliteEnemyFactory ? 80 : 150));
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
        for (BaseBullet bullet : heroBullets) bullet.forward();
        for (BaseBullet bullet : enemyBullets) bullet.forward();
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) enemyAircraft.forward();
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) prop.forward();
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
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) continue;
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();

                    if (enemyAircraft.notValid()) { // 敌机被击毁
                        score += 10;

                        // ---------- 播放敌机被击毁音效 ----------
                        if (ifMusicOn) {
                            MusicThread enemyDestroyedSound = new MusicThread("src/videos/bullet_hit.wav");
                            enemyDestroyedSound.start();
                        }

                        // ------------------- 掉落道具逻辑 -------------------
                        int maxDrop = 0;
                        if (enemyAircraft instanceof BossEnemy) {
                            maxDrop = 3; // Boss掉落数量上限3
                            bossExists = false; // Boss被击败，重置标志
                        } else if (enemyAircraft instanceof ElitePlus || enemyAircraft instanceof EliteEnemy) {
                            maxDrop = 1; // 精英敌机掉落1个
                        }

                        if (maxDrop > 0) {
                            int dropNum = (int)(Math.random() * (maxDrop + 1)); // 0~maxDrop 随机数量
                            for (int i = 0; i < dropNum; i++) {
                                double dropChance = Math.random();
                                PropFactory factory = null;
                                if (dropChance < 0.3) factory = new BloodPropFactory();
                                else if (dropChance < 0.6) factory = new BombPropFactory();
                                else if (dropChance < 0.85) factory = new FirePropFactory();
                                else factory = new SuperFirePropFactory();

                                if (factory != null) {
                                    props.add(factory.createProp(
                                            enemyAircraft.getLocationX(),
                                            enemyAircraft.getLocationY(),
                                            0,
                                            5
                                    ));
                                }
                            }
                        }
                    }
                }

                // 敌机与英雄机碰撞
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(50);
                }
            }
        }

// 英雄机获取道具
        for (AbstractProp prop : props) {
            if (!prop.notValid() && heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
                prop.vanish();

                // ---------- 播放道具生效音效 ----------
                if (ifMusicOn) {
                    MusicThread propSound = new MusicThread("src/videos/get_supply.wav");
                    propSound.start();
                }

                // 如果是炸弹道具，也可以播放炸弹音效
                if (prop instanceof BombProp) {
                    if (ifMusicOn) {
                        MusicThread bombSound = new MusicThread("src/videos/bomb_explosion.wav");
                        bombSound.start();
                    }
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
        // ✅ 使用不同难度传入的背景图
        g.drawImage(backgroundImage, 0, backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(backgroundImage, 0, backGroundTop, null);
        backGroundTop += 1;
        if (backGroundTop == Main.WINDOW_HEIGHT) backGroundTop = 0;

        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, enemyAircrafts);
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE,
                heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        paintScoreAndLife(g);
    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10, y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + score, x, y);
        y += 20;
        g.drawString("LIFE:" + heroAircraft.getHp(), x, y);
    }
}
