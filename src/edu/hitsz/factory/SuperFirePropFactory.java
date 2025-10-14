package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.SuperFireProp;

public class SuperFirePropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int x, int y, int speedX, int speedY) {
        return new SuperFireProp(x, y, speedX, speedY);
    }
}
