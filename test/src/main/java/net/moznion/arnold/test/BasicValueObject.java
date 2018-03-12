package net.moznion.arnold.test;

import java.util.List;
import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
class BasicValueObject {
    @Required
    private String foo;
    @Required
    private final double bar;
    @Required
    public int buz;
    @Required
    public final List<String> qux;

    private Object shouldBeIgnored;

    // Dummy constructor
    public BasicValueObject() {
        this.bar = 0;
        this.qux = null;
    }

    public String getFoo() {
        return foo;
    }

    public double getBar() {
        return bar;
    }
}
