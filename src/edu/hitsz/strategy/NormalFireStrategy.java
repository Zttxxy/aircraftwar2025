package edu.hitsz.strategy;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class NormalFireStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(HeroAircraft heroAircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = heroAircraft.getLocationX();
        int y = heroAircraft.getLocationY() - 2;
        int speedY = -10;
        int shootNum = heroAircraft.getShootNum();
        int power = heroAircraft.getPower();

        for (int i = 0; i < shootNum; i++) {
            int speedX = (i - shootNum / 2) * 2; // 扇形偏移
            BaseBullet bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }
}
