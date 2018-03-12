package net.moznion.arnold.test;

import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class InnerClassTestCase {
    @Required
    private Inner1 inner1;
    @Required
    private Inner2 inner2;
    @Required
    private String foo;

    @ArnoldBuilder
    static class Inner1 {
        @Required
        private String foo;
    }

    @ArnoldBuilder
    static class Inner2 {
        @Required
        private String foo;
    }
}
