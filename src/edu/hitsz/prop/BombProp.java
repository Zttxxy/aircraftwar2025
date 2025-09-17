package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BombProp extends AbstractProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
        vanish();
    }
}
