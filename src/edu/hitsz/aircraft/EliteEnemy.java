package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.observer.BombObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机（会射击）
 */
public class EliteEnemy extends AbstractAircraft implements BombObserver {

    private static final int SHOOT_NUM = 1;
    private static final int POWER = 30;
    private static final int DIRECTION = 1;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + DIRECTION * 2;
        int speedX = 0;
        int speedY = this.getSpeedY() + DIRECTION * 5;

        for (int i = 0; i < SHOOT_NUM; i++) {
            BaseBullet bullet = new EnemyBullet(
                    x + (i * 2 - SHOOT_NUM + 1) * 10,
                    y,
                    speedX,
                    speedY,
                    POWER
            );
            res.add(bullet);
        }
        return res;
    }

    @Override
    public void onBombExplode() {
        // 炸弹爆炸时，精英敌机直接消失
        this.vanish();
        System.out.println("精英敌机被炸弹清除");
    }
}