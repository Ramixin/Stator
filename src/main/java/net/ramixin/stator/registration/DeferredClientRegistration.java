package net.ramixin.stator.registration;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.ramixin.stator.networking.ClientPayloadContext;
import org.apache.commons.lang3.function.TriFunction;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public final class DeferredClientRegistration implements ClientRegistration, Deferred<ClientRegistration> {

    private final Queue<Consumer<ClientRegistration>> deferrals = new LinkedList<>();

    @Override
    public <T extends CustomPacketPayload> void clientboundHandler(CustomPacketPayload.Type<T> type, Consumer<ClientPayloadContext<T>> handler) {
        deferrals.add(registration -> registration.clientboundHandler(type, handler));
    }

    @Override
    public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void screen(Registrant<MenuType<M>> menuType, TriFunction<M, Inventory, Component, S> screenFactory) {
        deferrals.add(registration -> registration.screen(menuType, screenFactory));
    }

    @Override
    public void loadDeferred(DeferredClientRegistration registration) {
        deferrals.addAll(registration.deferrals);
    }

    @Override
    public Queue<Consumer<ClientRegistration>> deferrals() {
        return deferrals;
    }
}
