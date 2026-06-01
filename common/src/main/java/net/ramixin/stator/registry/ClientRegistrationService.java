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

import java.util.function.Consumer;

public interface ClientRegistrationService {

    <T extends CustomPacketPayload> void clientboundHandler(CustomPacketPayload.Type<T> type, Consumer<ClientPayloadHandlerContext<T>> handler);

    <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void screen(MenuType<M> menuType, TriFunction<M, Inventory, Component, S> screenFactory);
}
