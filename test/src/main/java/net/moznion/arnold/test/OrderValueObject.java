package net.moznion.arnold.test;

import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class OrderValueObject {
    @Required(order = 2)
    private int foo;
    @Required
    private int bar;
    @Required(order = 0)
    private int buz;
    @Required(order = 1)
    private int qux;
    private final int foobar;
    @Required(order = 1)
    private int buzqux;
    @Required(order = 999)
    private final int xxx;

    OrderValueObject() { // dummy
        this.foobar = 0;
        this.xxx = 0;
    }
}
