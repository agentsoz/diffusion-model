package socialnetwork.util;

import java.util.Random;

public class Global {

    // all application code should use this same instance of Random
    private static final Random random = new Random();

    private Global() {} // do not instantiate

    synchronized
    public static Random getRandom() {
        return random;
    }

    public static void setRandomSeed(long seed) {
        random.setSeed(seed);
    }
}
