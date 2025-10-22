// ScatterFireStrategy.java - 散射策略（三发子弹）
package edu.hitsz.strategy;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class ScatterFireStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(HeroAircraft heroAircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = heroAircraft.getLocationX();
        int y = heroAircraft.getLocationY() - 2;
        int speedY = -10;
        int power = heroAircraft.getPower();

        // 三发散射：中间直射，左右斜射
        res.add(new HeroBullet(x, y, 0, speedY, power));           // 中间
        res.add(new HeroBullet(x - 20, y, -2, speedY, power));    // 左斜
        res.add(new HeroBullet(x + 20, y, 2, speedY, power));     // 右斜

        return res;
    }
}