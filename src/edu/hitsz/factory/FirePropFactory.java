package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FireProp;

public class FirePropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int x, int y, int speedX, int speedY) {
        return new FireProp(x, y, 0, 5);
    }
}
