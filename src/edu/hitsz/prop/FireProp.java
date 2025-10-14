package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.NormalFireStrategy;

public class FireProp extends AbstractProp {

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active!");
        heroAircraft.setShootNum(3);
        heroAircraft.setPower(30);
        heroAircraft.setShootStrategy(new NormalFireStrategy());
        vanish();
    }
}
