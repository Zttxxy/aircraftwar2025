package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BloodProp extends AbstractProp {

    private int addHp = 30;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        heroAircraft.setHp(Math.min(heroAircraft.getHp() + addHp, heroAircraft.getMaxHp()));
        System.out.println("BloodSupply active! +HP");
        vanish();
    }
}
