package net.ramixin.stator.registry;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ramixin.stator.networking.PayloadHandlerContext;

import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class StatorRegistration {

    private static final RegistrationService SERVICE = ServiceLoader.load(RegistrationService.class, StatorRegistration.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo RegistrationService"));

    /**
     * @deprecated Use {@link #entry(Registry, Identifier, Supplier)} instead.
     */
    @Deprecated
    public static <T, V extends T> Registrant<V> register(Registry<T> registry, Identifier id, Supplier<V> realValue) {
        return entry(registry, id, realValue);
    }

    public static <T, V extends T> Registrant<V> entry(Registry<T> registry, Identifier id, Supplier<V> realValue) {
        return SERVICE.entry(registry, id, realValue);
    }

    public static <T extends CustomPacketPayload> void clientboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        SERVICE.clientboundPayload(type, codec);
    }

    public static <T extends CustomPacketPayload> void serverboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, Consumer<PayloadHandlerContext<T>> handler) {
        SERVICE.serverboundPayload(type, codec, handler);
    }

}
