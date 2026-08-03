package net.ramixin.stator.entrypoints;

import net.ramixin.stator.Platform;
import net.ramixin.stator.metadata.EntrypointsMetaFile;
import net.ramixin.stator.networking.ClientNetworking;
import net.ramixin.stator.networking.Networking;
import net.ramixin.stator.registration.ClientRegistration;
import net.ramixin.stator.registration.Registration;

import java.util.List;

public final class EntrypointHelper {

    public static void runEntrypoints(
            List<EntrypointsMetaFile.EntrypointData> data,
            ClientRegistration clientRegistration,
            Registration registration,
            ClientNetworking clientNetworking,
            Networking networking,
            Platform platform
    ) {
        for(EntrypointsMetaFile.EntrypointData entrypoint : data) {
            Object[] parameters = new Object[entrypoint.parameters().length];
            try {
                for(int i = 0; i < entrypoint.parameters().length; i++) {
                    switch(entrypoint.parameters()[i]) {
                        case CLIENT_REGISTRATION -> parameters[i] = clientRegistration;
                        case REGISTRATION -> parameters[i] = registration;
                        case CLIENT_NETWORKING -> parameters[i] = clientNetworking;
                        case NETWORKING -> parameters[i] = networking;
                        case PLATFORM -> parameters[i] = platform;
                        default -> throw new RuntimeException("Unsupported parameter type: " + entrypoint.parameters()[i]);
                    }
                }
                entrypoint.method().invoke(null, parameters);
            } catch (Exception e) {
                throw new RuntimeException("Failed to run entrypoint", e);
            }
        }
    }

}
