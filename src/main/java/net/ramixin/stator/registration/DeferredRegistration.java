package net.ramixin.stator.registration;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ramixin.stator.networking.PayloadContext;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DeferredRegistration implements Registration, Deferred<Registration> {

    private final Queue<Consumer<Registration>> deferrals = new ArrayDeque<>();

    @Override
    public <T, V extends T> Registrant<V> entry(Registry<T> registry, Identifier id, Supplier<V> realValue) {
        DeferredRegistrant<V> deferral = new DeferredRegistrant<>(reg -> reg.entry(registry, id, realValue));
        deferrals.add(deferral);
        return deferral;
    }

    @Override
    public <T extends CustomPacketPayload> void clientboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        deferrals.add(reg -> reg.clientboundPayload(type, codec));
    }

    @Override
    public <T extends CustomPacketPayload> void serverboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, Consumer<PayloadContext<T>> handler) {
        deferrals.add(reg -> reg.serverboundPayload(type, codec, handler));
    }

    @Override
    public void loadDeferred(DeferredRegistration deferred) {
        this.deferrals.addAll(deferred.deferrals);
    }

    @Override
    public Queue<Consumer<Registration>> deferrals() {
        return deferrals;
    }
}
