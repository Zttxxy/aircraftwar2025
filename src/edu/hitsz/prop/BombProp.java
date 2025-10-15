package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.List;

public class BombProp extends AbstractProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY); // ✅ 调用父类构造函数
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        System.out.println("BombProp active() basic version called.");
    }

    @Override
    public void active(HeroAircraft heroAircraft, List<AbstractAircraft> enemies, List<BaseBullet> bullets) {
        bullets.clear();
        for (AbstractAircraft enemy : enemies) {
            enemy.vanish();
        }
        System.out.println("BombProp active() cleared all enemies and bullets!");
    }
}
