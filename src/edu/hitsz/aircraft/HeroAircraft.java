package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.strategy.NormalFireStrategy;

import java.util.List;

/**
 * 英雄飞机（单例 + 策略模式）
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

    /** 射击策略 */
    private ShootStrategy shootStrategy = new NormalFireStrategy(); // 默认普通火力


    /** 新增分数属性 */
    private int score = 0;


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
        // 英雄机前进逻辑，如果需要可添加
    }

    /**
     * 英雄机射击，使用当前射击策略
     * @return 射击出的子弹列表
     */
    @Override
    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this);
    }


    // ---------------- score 相关 ----------------
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }



    // ---------------- 道具或外部接口 ----------------
    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getShootNum() {
        return shootNum;
    }

    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public ShootStrategy getShootStrategy() {
        return shootStrategy;
    }

    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }
}
