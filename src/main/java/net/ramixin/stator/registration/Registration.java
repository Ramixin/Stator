package net.ramixin.stator.registration;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ramixin.stator.networking.PayloadHandlerContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Registration {

    <T, V extends T> Registrant<V> entry(Registry<T> registry, Identifier id, Supplier<V> realValue);

    <T extends CustomPacketPayload> void clientboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec);

    <T extends CustomPacketPayload> void serverboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, Consumer<PayloadHandlerContext<T>> handler);

}
