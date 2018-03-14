package net.moznion.arnold.exception;

/**
 * ArnoldAnnotationProcessingFailedException raises when the phase of annotation processing is
 * failed.
 */
public class ArnoldAnnotationProcessingFailedException extends RuntimeException {
    private static final long serialVersionUID = 6084910476216448151L;

    public ArnoldAnnotationProcessingFailedException(final Throwable e) {
        super(e);
    }
}
