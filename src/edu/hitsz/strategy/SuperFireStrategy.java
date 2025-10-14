package edu.hitsz.strategy;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class SuperFireStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(HeroAircraft heroAircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = heroAircraft.getLocationX();
        int y = heroAircraft.getLocationY() - 2;
        int shootNum = 20; // 环形子弹数量
        int speed = 8;
        int power = heroAircraft.getPower();

        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI / shootNum * i;
            int vx = (int) (speed * Math.cos(angle));
            int vy = (int) (speed * Math.sin(angle));
            res.add(new HeroBullet(x, y, vx, vy, power));
        }
        return res;
    }
}
