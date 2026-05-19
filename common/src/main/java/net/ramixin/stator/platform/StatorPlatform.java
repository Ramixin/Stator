package net.ramixin.stator.platform;

import java.nio.file.Path;
import java.util.ServiceLoader;

public final class StatorPlatform {

    private static final PlatformService SERVICE = ServiceLoader.load(PlatformService.class, StatorPlatform.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo StatorPlatform service"));

    public static String getPlatformName() {
        return SERVICE.getPlatformName();
    }

    public static Path getGameDirectory() {
        return SERVICE.getGameDirectory();
    }

    public static boolean isDevEnv() {
        return SERVICE.isDevEnv();
    }
}
