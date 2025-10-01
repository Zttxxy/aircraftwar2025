package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;

public class BombPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int x, int y, int speedX, int speedY) {
        return new BombProp(x, y, 0, 5);
    }
}
