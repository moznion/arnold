package net.moznion.arnold;

import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class SingleFieldTestCase {
    @Required
    private String foo;
}
