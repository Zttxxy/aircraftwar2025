package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板，游戏启动
 */
public class Game extends JPanel {

    private final List<AbstractProp> props;

    private int backGroundTop = 0;
    private final ScheduledExecutorService executorService;
    private int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;

    private int enemyMaxNumber = 3;
    private int score = 0;
    private int time = 0;
    private int cycleDuration = 600;
    private int cycleTime = 0;
    private boolean gameOverFlag = false;

    // Boss相关变量
    private int bossThreshold = 200;
    private int nextBossScore = bossThreshold;
    private boolean bossExists = false;

    public Game() {
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

    public void action() {
        Runnable task = () -> {
            time += timeInterval;

            // 周期性事件（敌机生成 + 射击）
            if (timeCountAndNewCycleJudge()) {

                // Boss 出现条件：分数达到阈值 & 当前没有Boss
                boolean bossExists = enemyAircrafts.stream().anyMatch(e -> e instanceof BossEnemy);
                if (score >= nextBossScore && !bossExists) {
                    EnemyFactory bossFactory = new BossEnemyFactory();
                    enemyAircrafts.add(bossFactory.createEnemy(
                            Main.WINDOW_WIDTH / 2,
                            (int) (Main.WINDOW_HEIGHT * 0.05),
                            2, 0, 500
                    ));
                    System.out.println("Boss 出现！");
                    nextBossScore += bossThreshold;
                }

                // ------------------- 敌机生成 -------------------
                int initialEnemyNumber = 2; // 刚开始生成敌机数量
                while (enemyAircrafts.size() < Math.min(enemyMaxNumber, initialEnemyNumber)) {
                    addRandomEnemy();
                }

                while (enemyAircrafts.size() < enemyMaxNumber) {
                    double rand = Math.random();
                    if (rand < 0.6) { // 普通敌机
                        addRandomEnemy();
                    } else if (rand < 0.85) { // 精英敌机
                        addRandomEnemy(new EliteEnemyFactory());
                    } else { // 超级精英
                        addRandomEnemy(new ElitePlusFactory());
                    }
                }

                // 敌机和英雄机射击
                shootAction();
            }

            // 每帧都执行的事件
            bulletsMoveAction();
            aircraftsMoveAction();
            propsMoveAction();
            crashCheckAction();
            postProcessAction();
            repaint();

            // 游戏结束检查
            if (heroAircraft.getHp() <= 0) {
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");

                // ---------------- 游戏结束时记录并输出排行榜 ----------------
                int finalScore = score;
                PlayerDaoImpl dao = new PlayerDaoImpl();
                dao.addPlayer(new Player("Player1", finalScore, PlayerDaoImpl.currentTime()));
                dao.sortPlayersByScore();
                dao.printRanking();
                dao.saveToFile("ranking.txt");
            }
        };

        // 每40ms执行一次循环
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    // ------------------- 辅助方法 -------------------
    private void addRandomEnemy() {
        addRandomEnemy(new MobEnemyFactory());
    }

    private void addRandomEnemy(EnemyFactory factory) {
        int enemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
        if (factory instanceof EliteEnemyFactory || factory instanceof ElitePlusFactory) {
            enemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();
        }

        int x = getNonOverlappingX(enemyWidth);
        int y = 20 + (int)(Math.random() * 30); // 顶部留一些间距
        enemyAircrafts.add(factory.createEnemy(x, y, 3, 5, 30));
    }

    // 避免水平重叠
    private int getNonOverlappingX(int enemyWidth) {
        int x;
        boolean overlap;
        int attempts = 0;
        do {
            x = (int) (Math.random() * (Main.WINDOW_WIDTH - enemyWidth));
            overlap = false;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (Math.abs(enemy.getLocationX() - x) < enemyWidth + 10) { // 10像素间隔
                    overlap = true;
                    break;
                }
            }
            attempts++;
        } while (overlap && attempts < 10);
        return x;
    }



    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    private void shootAction() {
        // 英雄射击
        heroBullets.addAll(heroAircraft.shoot());

        // 敌机射击：只有精英敌机、超级精英和Boss能射击
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (!enemy.notValid()) {
                enemyBullets.addAll(enemy.shoot());
            }
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
                    if (enemyAircraft.notValid()) {
                        score += 10;

                        // 根据敌机类型掉落道具
                        int maxDrop = 0;
                        if (enemyAircraft instanceof BossEnemy) {
                            maxDrop = 3;
                            bossExists = false; // Boss被击败，重置标志
                        } else if (enemyAircraft instanceof ElitePlus) {
                            maxDrop = 1;
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            maxDrop = 1;
                        }

                        // 掉落道具
                        if (maxDrop > 0) {
                            double dropChance = Math.random();
                            PropFactory factory = null;
                            if (dropChance < 0.3) {
                                factory = new BloodPropFactory();
                            } else if (dropChance < 0.6) {
                                factory = new BombPropFactory();
                            } else if (dropChance < 0.85) {
                                factory = new FirePropFactory();
                            } else {
                                factory = new SuperFirePropFactory(); // 超级火力道具
                            }

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

                // 敌机与英雄机碰撞
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(50); // 碰撞造成50点伤害
                }
            }
        }

        // 英雄机获取道具
        for (AbstractProp prop : props) {
            if (!prop.notValid() && heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
                prop.vanish();
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

        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop, null);
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
        if (objects.isEmpty()) return;
        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null;
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

    // 工厂模式生成道具
    private void dropProp(int x, int y, int maxCount) {
        int dropNum = (int)(Math.random() * (maxCount + 1));
        List<PropFactory> propFactories = Arrays.asList(
                new BloodPropFactory(),
                new BombPropFactory(),
                new FirePropFactory(),
                new SuperFirePropFactory()
        );
        for (int i = 0; i < dropNum; i++) {
            int index = (int)(Math.random() * propFactories.size());
            AbstractProp prop = propFactories.get(index).createProp(x, y, 0, 5);
            props.add(prop);
        }
    }
}