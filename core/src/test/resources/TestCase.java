package net.moznion.arnold;

import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class TestCase {
    private final String hoge;
    @Required
    private Long fuga;
    @Required
    private final int piyo;

    private String shouldIgnore;

    private TestCase() {
        hoge = "";
        fuga = 0L;
        piyo = 0;
    }
}
