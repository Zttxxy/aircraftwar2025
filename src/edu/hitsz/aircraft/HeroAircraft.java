package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机（单例）
 */
public class HeroAircraft extends AbstractAircraft {

    // ---------------- 单例相关 ----------------
    private volatile static HeroAircraft instance = null;

    // ---------------- 英雄机功能属性 ----------------
    /** 最大血量 */
    private final int maxHp;

    /** 子弹一次发射数量 */
    private int shootNum = 1;

    /** 子弹伤害 */
    private int power = 30;

    /** 子弹射击方向 (向上发射：-1，向下发射：1) */
    private int direction = -1;

    /**
     * 私有构造函数，外部不可 new
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.maxHp = hp; // 初始化最大血量
    }

    /**
     * 带参数的 getInstance：首次调用时创建对象（线程安全，DCL）
     */
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            synchronized (HeroAircraft.class) {
                if (instance == null) {
                    instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
                }
            }
        }
        return instance;
    }

    /**
     * 无参 getInstance：获取已创建好的单例（若未初始化则抛异常）
     */
    public static HeroAircraft getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HeroAircraft not initialized. Call getInstance(x,y,speedX,speedY,hp) first.");
        }
        return instance;
    }

    @Override
    public void forward() {
    }

    @Override
    /**
     * 通过射击产生子弹
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = this.getSpeedY() + direction * 10;
        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }

    // 道具/外部修改生命值的接口
    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }
}
