package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.observer.BombObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * 普通敌机
 * 不可射击
 */
public class MobEnemy extends AbstractAircraft implements BombObserver {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
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
        return new LinkedList<>();
    }

    @Override
    public void onBombExplode() {
        // 炸弹爆炸时，普通敌机直接消失
        this.vanish();
        System.out.println("普通敌机被炸弹清除");
    }
}