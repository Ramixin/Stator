package net.ramixin.stator.events.contexts;

import net.minecraft.world.entity.player.Player;

public interface PlayerJoinedServerContext extends Context {

    Player player();

}
