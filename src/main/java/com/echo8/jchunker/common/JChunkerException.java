package com.echo8.jchunker.common;

/**
 * <p>
 * The exception thrown when an error happens inside JChunker.
 * </p>
 */
public class JChunkerException extends Exception {
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 8819064310840042957L;
    
    public JChunkerException() {}
    
    /**
     * 
     * @param message
     *              A description of the error.
     */
    public JChunkerException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param cause
     *              The exception that caused the error.
     */
    public JChunkerException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 
     * @param message
     *              A description of the error.
     * @param cause
     *              The exception that caused the error.
     */
    public JChunkerException(String message, Throwable cause) {
        super(message, cause);
    }
}
