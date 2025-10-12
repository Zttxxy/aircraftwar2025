package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ElitePlus extends AbstractAircraft {

    private int power = 30;
    private int direction = 1; // 横向移动方向
    private int shootNum = 3; // 一次发射3颗子弹

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
            // 扇形发射，左右偏移
            BaseBullet bullet = new EnemyBullet(x + (i - 1) * 20, y, (i - 1) * speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }
}
