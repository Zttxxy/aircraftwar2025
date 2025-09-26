package edu.hitsz.application;

import edu.hitsz.aircraft.HeroAircraft;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 英雄机控制类
 * 监听鼠标，控制英雄机的移动
 */
public class HeroController {
    private Game game;
    private HeroAircraft heroAircraft;
    private MouseAdapter mouseAdapter;

    // [MODIFIED] 仅传入 Game，heroAircraft 从单例中获取
    public HeroController(Game game){
        this.game = game;
        // 直接获取已经在 Game 构造中初始化过的单例
        this.heroAircraft = HeroAircraft.getInstance();

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                int x = e.getX();
                int y = e.getY();
                if ( x<0 || x>Main.WINDOW_WIDTH || y<0 || y>Main.WINDOW_HEIGHT){
                    // 防止超出边界
                    return;
                }
                heroAircraft.setLocation(x, y);
            }
        };

        game.addMouseListener(mouseAdapter);
        game.addMouseMotionListener(mouseAdapter);
    }
}
