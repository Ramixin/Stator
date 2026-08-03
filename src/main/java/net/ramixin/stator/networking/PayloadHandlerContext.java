package net.ramixin.stator.networking;

import net.minecraft.world.entity.player.Player;

public interface PayloadHandlerContext<T> {

    T payload();

    Player player();

}
