package net.ramixin.stator.registry;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.ramixin.stator.networking.ClientPayloadHandlerContext;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public final class StatorClientRegistration {

    private static final ClientRegistrationService SERVICE = ServiceLoader.load(ClientRegistrationService.class, StatorClientRegistration.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo ClientRegistrationService"));

    public static <T extends CustomPacketPayload> void clientboundHandler(CustomPacketPayload.Type<T> type, Consumer<ClientPayloadHandlerContext<T>> handler) {
        SERVICE.clientboundHandler(type, handler);
    }

    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void screen(Registrant<MenuType<M>> menuType, TriFunction<M, Inventory, Component, S> screenFactory) {
        SERVICE.screen(menuType, screenFactory);
    }
}
