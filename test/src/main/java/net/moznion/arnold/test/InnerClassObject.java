package net.moznion.arnold.test;

import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
class InnerClassObject {
    @Required
    private Inner1 inner1;
    @Required
    private Inner2 inner2;
    @Required
    private String foo;

    public Inner1 getInner1() {
        return inner1;
    }

    public Inner2 getInner2() {
        return inner2;
    }

    public String getFoo() {
        return foo;
    }

    @ArnoldBuilder
    static class Inner1 {
        @Required
        private String foo;

        public String getFoo() {
            return foo;
        }
    }

    @ArnoldBuilder
    static class Inner2 {
        @Required
        private String foo;

        public String getFoo() {
            return foo;
        }
    }
}
