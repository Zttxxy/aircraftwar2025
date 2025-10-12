package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
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

    private int enemyMaxNumber = 5;
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
                0, 0, 100000);

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
                    // 生成 Boss
                    EnemyFactory bossFactory = new BossEnemyFactory();
                    enemyAircrafts.add(bossFactory.createEnemy(
                            Main.WINDOW_WIDTH / 2,
                            (int) (Main.WINDOW_HEIGHT * 0.05),
                            2, 0, 500
                    ));
                    System.out.println("Boss 出现！");
                    // 更新下一次Boss的触发分数
                    nextBossScore += bossThreshold;
                }

                // 普通敌机生成逻辑
                // 1. 保证场上至少有 1 个敌机
                if (enemyAircrafts.isEmpty()) {
                    EnemyFactory mobFactory = new MobEnemyFactory();
                    enemyAircrafts.add(mobFactory.createEnemy(
                            (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                            (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                            3, 5, 30
                    ));
                }

                // 2. 补充敌机直到达到上限
                while (enemyAircrafts.size() < enemyMaxNumber) {
                    double rand = Math.random();
                    if (rand < 0.6) { // 普通敌机
                        EnemyFactory mobFactory = new MobEnemyFactory();
                        enemyAircrafts.add(mobFactory.createEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                3, 5, 30
                        ));
                    } else if (rand < 0.85) { // 精英敌机
                        EnemyFactory eliteFactory = new EliteEnemyFactory();
                        enemyAircrafts.add(eliteFactory.createEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                3, 4, 80
                        ));
                    } else { // 超级精英敌机 (你的ElitePlus)
                        EnemyFactory elitePlusFactory = new ElitePlusFactory();
                        enemyAircrafts.add(elitePlusFactory.createEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                3, 3, 150
                        ));
                    }
                }

                // 敌机和英雄机射击
                shootAction();
            }

            // 每帧都执行的事件
            bulletsMoveAction();    // 子弹移动
            aircraftsMoveAction();  // 飞机和道具移动
            propsMoveAction();      // 道具移动
            crashCheckAction();     // 碰撞检测
            postProcessAction();    // 删除无效对象
            repaint();              // 重绘界面

            // 游戏结束检查
            if (heroAircraft.getHp() <= 0) {
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }
        };

        // 每40ms执行一次循环
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
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
                            dropProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY(), maxDrop);
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
                new FirePropFactory()
        );
        for (int i = 0; i < dropNum; i++) {
            int index = (int)(Math.random() * propFactories.size());
            AbstractProp prop = propFactories.get(index).createProp(x, y, 0, 5);
            props.add(prop);
        }
    }
}