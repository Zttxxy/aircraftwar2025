package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;

import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    /**
     * 最大生命值（构造时指定）
     */
    protected int maxHp;
    /**
     * 当前生命值
     */
    protected int hp;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    /**
     * 减少生命值
     */
    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish(); // 血量归零，飞机消失
        }
    }

    /**
     * 增加生命值（不超过最大生命值）
     */
    public void increaseHp(int increase) {
        hp += increase;
        if (hp > maxHp) {
            hp = maxHp; // 不超过最大血量
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 飞机射击方法，可射击对象必须实现
     *
     * @return 可射击对象需实现，返回子弹；非可射击对象空实现，返回null
     */
    public abstract List<BaseBullet> shoot();

}
