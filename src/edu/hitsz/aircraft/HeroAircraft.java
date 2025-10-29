package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.strategy.NormalFireStrategy;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    // ---------------- 火力增强相关 ----------------
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask;


    /**
     * 私有构造函数，外部不可 new
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.maxHp = hp; // 初始化最大血量
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
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

    // ---------------- 火力增强相关方法 ----------------

    /**
     * 设置临时射击策略
     * @param strategy 新的射击策略
     * @param durationMillis 持续时间（毫秒）
     */
    public void setTemporaryStrategy(ShootStrategy strategy, long durationMillis) {
        // 安全检查：确保调度器没有关闭
        if (scheduler == null || scheduler.isShutdown()) {
            System.out.println("调度器已关闭，无法设置临时策略");
            return;
        }

        // 取消之前的任务（如果存在）
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            try {
                currentTask.get(100, TimeUnit.MILLISECONDS); // 等待任务取消完成
            } catch (Exception e) {
                // 忽略取消时的异常
            }
        }

        ShootStrategy originalStrategy = this.shootStrategy;
        this.shootStrategy = strategy;

        System.out.println("设置临时策略: " + strategy.getClass().getSimpleName() +
                ", 持续时间: " + durationMillis + "ms");

        // 安排恢复原始策略的任务
        try {
            currentTask = scheduler.schedule(() -> {
                // 再次检查调度器状态和游戏状态
                if (!scheduler.isShutdown()) {
                    this.shootStrategy = new NormalFireStrategy();
                    System.out.println("恢复普通射击模式");
                }
            }, durationMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.err.println("安排策略恢复任务时出错: " + e.getMessage());
            // 如果安排任务失败，立即恢复普通模式
            this.shootStrategy = new NormalFireStrategy();
        }
    }


    /**
     * 获取当前策略名称（用于UI显示）
     */
    public String getCurrentStrategyName() {
        return shootStrategy.getClass().getSimpleName();
    }

    /**
     * 清理资源（游戏结束时调用）
     */
    public void cleanup() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true); // 使用 true 确保立即中断
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // 立即关闭
            try {
                if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    System.out.println("Scheduler did not terminate");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 重置英雄机状态（开始新游戏时调用）
     */
    public void reset() {
        // 先清理现有任务
        cleanup();

        // 重新初始化调度器
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // 重置状态
        this.shootStrategy = new NormalFireStrategy();
        this.hp = maxHp;
        this.score = 0;
        this.shootNum = 1;
        this.power = 30;
        this.direction = -1;

        // 重置位置
        this.setLocation(Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight());

        System.out.println("英雄机状态已重置");
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