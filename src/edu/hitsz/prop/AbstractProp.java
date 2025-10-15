package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.List;

public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    // 普通道具调用
    public abstract void active(HeroAircraft heroAircraft);

    // 炸弹道具调用（可以重载，不用 @Override）
    public void active(HeroAircraft heroAircraft, List<AbstractAircraft> enemies, List<BaseBullet> bullets) {
        active(heroAircraft); // 默认调用单参数版本
    }
}
