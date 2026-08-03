package net.ramixin.stator;

import java.nio.file.Path;

public interface Platform {

    String FABRIC = "fabric";
    String NEOFORGE = "neoforge";

    String platformName();

    Path gameDirectory();

    boolean isDevEnv();

}
