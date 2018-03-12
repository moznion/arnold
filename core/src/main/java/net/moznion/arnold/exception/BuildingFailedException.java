package net.moznion.arnold.exception;

public class BuildingFailedException extends RuntimeException {
    private static final long serialVersionUID = -3463617751399590987L;

    public BuildingFailedException(final Exception e) {
        super(e);
    }
}
