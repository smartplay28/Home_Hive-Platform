package com.example.UC_Backend.Extra;

import java.util.Random;

/**
 * This class is used for generating random ID's
 */

public class ExtraFunctions {

    public int generateID() {
        // Generates a robust, positive integer ID derived from a true UUID.
        // This avoids changing `int` to `String` across 25+ files while providing
        // a massive collision-space improvement over the old random 5-digit number.
        return Math.abs(java.util.UUID.randomUUID().hashCode());
    }
}
