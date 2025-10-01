package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;

public class BloodPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int x, int y,int speedX, int speedY) {
        return new BloodProp(x, y, 0, 5);
    }
}
