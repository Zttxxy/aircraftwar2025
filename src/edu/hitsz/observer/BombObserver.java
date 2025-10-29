package edu.hitsz.observer;

/**
 * 炸弹观察者接口
 * 所有需要响应炸弹爆炸的对象实现此接口
 */
public interface BombObserver {
    /**
     * 当炸弹爆炸时被调用
     */
    void onBombExplode();
}