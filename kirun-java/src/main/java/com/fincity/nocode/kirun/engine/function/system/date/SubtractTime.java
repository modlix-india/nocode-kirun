package com.fincity.nocode.kirun.engine.function.system.date;

public class SubtractTime extends AddTime {

    public SubtractTime() {
        super("SubtractTime", "subtract");
    }

    @Override
    public int setFunction(int actual, int subValue) {
        return actual - subValue;
    }
}
