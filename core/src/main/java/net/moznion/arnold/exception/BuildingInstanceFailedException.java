package net.moznion.arnold.exception;

import net.moznion.arnold.annotation.ArnoldBuilder;

/**
 * BuildingInstanceFailedException raises when instantiating via {@link ArnoldBuilder} is failed.
 */
public class BuildingInstanceFailedException extends RuntimeException {
    private static final long serialVersionUID = -3463617751399590987L;

    public BuildingInstanceFailedException(final Exception e) {
        super(e);
    }
}
