package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombSubject;

import java.util.List;

public class BombProp extends AbstractProp {

    private BombSubject bombSubject;

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.bombSubject = new BombSubject();
    }

    /**
     * 添加观察者
     */
    public void attachObserver(BombObserver observer) {
        bombSubject.attach(observer);
    }

    /**
     * 移除观察者
     */
    public void detachObserver(BombObserver observer) {
        bombSubject.detach(observer);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        System.out.println("BombProp active() called with observer pattern");
        // 通知所有观察者（敌机和子弹）
        bombSubject.notifyAllObservers();
        System.out.println("炸弹爆炸，清除了 " + bombSubject.getObserverCount() + " 个目标");
    }
}