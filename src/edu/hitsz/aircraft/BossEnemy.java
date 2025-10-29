package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.observer.BombObserver;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends AbstractAircraft implements BombObserver {

    private int shootNum = 20;
    private int power = 15;
    private int direction = 1;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + 2;
        int speed = 5;

        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI / shootNum * i;
            int vx = (int) (speed * Math.cos(angle));
            int vy = (int) (speed * Math.sin(angle));
            BaseBullet bullet = new EnemyBullet(x, y, vx, vy, power);
            res.add(bullet);
        }
        return res;
    }

    @Override
    public void onBombExplode() {
        // Boss敌机不受炸弹影响
        System.out.println("Boss敌机免疫炸弹效果");
    }
}