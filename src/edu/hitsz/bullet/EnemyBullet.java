package edu.hitsz.bullet;

import edu.hitsz.observer.BombObserver;

/**
 * 敌机子弹
 */
public class EnemyBullet extends BaseBullet implements BombObserver {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public void onBombExplode() {
        // 炸弹爆炸时，敌机子弹消失
        this.vanish();
    }
}