package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
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

    // [MODIFIED] 添加道具列表
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

    public Game() {
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 100);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>(); // [MODIFIED]

        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        new HeroController(this);
    }

    public void action() {

        Runnable task = () -> {
            time += timeInterval;

            if (timeCountAndNewCycleJudge()) {
                if (enemyAircrafts.size() < enemyMaxNumber) {
                    enemyAircrafts.add(new MobEnemy(
                            (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                            (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                            0,
                            10,
                            30
                    ));
                }
                shootAction();
            }

            bulletsMoveAction();
            aircraftsMoveAction();
            propsMoveAction(); // [MODIFIED] 道具下落

            crashCheckAction();
            postProcessAction();
            repaint();

            if (heroAircraft.getHp() <= 0) {
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }
        };

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
        // TODO: 敌机射击
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    // [MODIFIED] 道具下落方法
    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    // [MODIFIED] 道具掉落方法
    private void dropProp(int x, int y) {
        double dropRate = 0.3; // 30%概率掉落
        if (Math.random() > dropRate) {
            return;
        }

        int type = (int) (Math.random() * 3);
        AbstractProp prop;
        switch (type) {
            case 0:
                prop = new BloodProp(x, y, 0, 5);
                break;
            case 1:
                prop = new BombProp(x, y, 0, 5);
                break;
            default:
                prop = new FireProp(x, y, 0, 5);
                break;
        }
        props.add(prop);
    }

    private void crashCheckAction() {
        // TODO: 敌机子弹攻击英雄

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
                        dropProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY()); // [MODIFIED] 敌机击毁后可能掉落道具
                    }
                }
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // [MODIFIED] 我方获得道具
        for (AbstractProp prop : props) {
            if (!prop.notValid() && heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
            }
        }
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid); // [MODIFIED]
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop, null);
        backGroundTop += 1;
        if (backGroundTop == Main.WINDOW_HEIGHT) {
            backGroundTop = 0;
        }

        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, enemyAircrafts);

        // [MODIFIED] 绘制道具
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
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
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + score, x, y);
        y += 20;
        g.drawString("LIFE:" + heroAircraft.getHp(), x, y);
    }
}
