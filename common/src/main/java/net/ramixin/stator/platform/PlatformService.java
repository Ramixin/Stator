package net.ramixin.stator.platform;

import java.nio.file.Path;

public interface PlatformService {

    String getPlatformName();

    Path getGameDirectory();

    boolean isDevEnv();

}
