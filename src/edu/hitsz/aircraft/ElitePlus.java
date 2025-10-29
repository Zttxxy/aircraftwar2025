package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.observer.BombObserver;

import java.util.LinkedList;
import java.util.List;

public class ElitePlus extends AbstractAircraft implements BombObserver {

    private int power = 30;
    private int direction = 1;
    private int shootNum = 3;

    public ElitePlus(int locationX, int locationY, int speedX, int speedY, int hp){
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + 2;
        int speedX = 2;
        int speedY = this.getSpeedY() + direction * 5;

        for (int i = 0; i < shootNum; i++) {
            BaseBullet bullet = new EnemyBullet(x + (i - 1) * 20, y, (i - 1) * speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }

    @Override
    public void onBombExplode() {
        // 炸弹爆炸时，超级精英敌机血量减少（但不消失）
        this.decreaseHp(100); // 减少100点血量
        System.out.println("超级精英敌机受到炸弹伤害，剩余血量: " + this.getHp());
    }
}