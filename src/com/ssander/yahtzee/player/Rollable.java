package com.ssander.yahtzee.player;

/**
 * Interface for objects that can be rolled to generate random values.
 * Primarily implemented by dice in the Yahtzee game.
 */
public interface Rollable {
    /**
     * Performs a roll action to generate a new random value.
     */
    void roll();
}
