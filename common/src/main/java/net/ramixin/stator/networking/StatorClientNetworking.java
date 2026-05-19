package net.ramixin.stator.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public final class StatorClientNetworking {

    private static final ClientNetworkingService SERVICE = ServiceLoader.load(ClientNetworkingService.class, StatorClientNetworking.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo ClientNetworking service"));

    public static void sendServerbound(CustomPacketPayload payload) {
        SERVICE.sendServerbound(payload);
    }

    public static <T extends CustomPacketPayload> void registerClientboundHandler(CustomPacketPayload.Type<T> type, Consumer<ClientPayloadHandlerContext<T>> handler) {
        SERVICE.registerClientboundHandler(type, handler);
    }

}
