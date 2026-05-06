package com.example.UC_Backend.Extra;

public class RangeChecker {
    private static volatile boolean loaded = false;
    private static final Object lock = new Object();

    static {
        synchronized (lock) {
            if (!loaded) {
                String libraryPath = RangeChecker.class.getResource("/lib/libRangeChecker.dylib").getPath();
                System.load(libraryPath);
                loaded = true;
            }
        }
    }

    public native int getAgentsInRange(String CustomerLocation, String AgentLocation);

    
}
