package net.ramixin.stator.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public final class StatorNetworking {

    private static final NetworkingService SERVICE = ServiceLoader.load(NetworkingService.class, StatorNetworking.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo NetworkingService service"));

    public static <T extends CustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        SERVICE.registerClientbound(type, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, Consumer<PayloadHandlerContext<T>> handler) {
        SERVICE.registerServerbound(type, codec, handler);
    }

    public static void sendClientbound(ServerPlayer player, CustomPacketPayload payload) {
        SERVICE.sendClientbound(player, payload);
    }
}
