package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends AbstractAircraft {

    private int shootNum = 20; // 一次发射20颗子弹
    private int power = 15;    // 攻击力
    private int direction = 1;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + 2;
        int speed = 5; // 子弹飞行速度

        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI / shootNum * i; // 环形分布
            int vx = (int) (speed * Math.cos(angle));
            int vy = (int) (speed * Math.sin(angle));
            BaseBullet bullet = new EnemyBullet(x, y, vx, vy, power);
            res.add(bullet);
        }
        return res;
    }
}
