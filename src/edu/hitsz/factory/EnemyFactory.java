package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;

public interface EnemyFactory {
    AbstractAircraft createEnemy(int x, int y, int speedX, int speedY, int hp);
}
