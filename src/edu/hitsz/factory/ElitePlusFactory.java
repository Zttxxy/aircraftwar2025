package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ElitePlus;

public class ElitePlusFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int x, int y, int speedX, int speedY, int hp){
        return new ElitePlus(x, y, speedX, speedY, hp);
    }
}
