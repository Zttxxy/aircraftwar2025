package edu.hitsz.observer;

import java.util.LinkedList;
import java.util.List;

/**
 * 炸弹主题（被观察者）
 * 管理所有观察炸弹爆炸的对象
 */
public class BombSubject {
    private List<BombObserver> observers = new LinkedList<>();

    /**
     * 添加观察者
     */
    public void attach(BombObserver observer) {
        observers.add(observer);
    }

    /**
     * 移除观察者
     */
    public void detach(BombObserver observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有观察者
     */
    public void notifyAllObservers() {
        // 使用新列表避免在迭代过程中修改原列表
        List<BombObserver> observersCopy = new LinkedList<>(observers);
        for (BombObserver observer : observersCopy) {
            observer.onBombExplode();
        }
    }

    /**
     * 获取观察者数量
     */
    public int getObserverCount() {
        return observers.size();
    }
}