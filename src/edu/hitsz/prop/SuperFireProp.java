package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.strategy.SuperFireStrategy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;

public class SuperFireProp extends AbstractProp {

    private static final int SUPER_FIRE_DURATION = 8000; // 超级火力持续8秒

    public SuperFireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        System.out.println("SuperFireProp active!");
        heroAircraft.setPower(40); // 更高伤害
        heroAircraft.setShootNum(5); // 可选：增加发射颗数
        heroAircraft.setShootStrategy(new SuperFireStrategy());
        vanish();

        // 保存原始属性
        int originalShootNum = heroAircraft.getShootNum();
        int originalPower = heroAircraft.getPower();

        // 设置超级火力
        heroAircraft.setShootNum(originalShootNum * 3);
        heroAircraft.setPower(originalPower * 2);

        // 创建定时器，到期恢复原来的属性
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            heroAircraft.setShootNum(originalShootNum);
            heroAircraft.setPower(originalPower);
            executor.shutdown();
        }, SUPER_FIRE_DURATION, TimeUnit.MILLISECONDS);
    }

    @Override
    public BufferedImage getImage() {
        return ImageManager.SUPER_FIRE_PROP_IMAGE;
    }
}
