package com.ssander.yahtzee.ui;

/**
 * Thrown by a {@link GameUI} when no more input can be read
 * (for example the console was closed or input was piped and ran out).
 * The game catches it and ends cleanly instead of crashing.
 */
public class InputClosedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Input closed");
    }
}
