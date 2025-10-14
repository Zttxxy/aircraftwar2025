package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.aircraft.HeroAircraft;

import java.util.List;

public interface ShootStrategy {
    List<BaseBullet> shoot(HeroAircraft heroAircraft);
}
