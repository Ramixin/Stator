package net.ramixin.stator.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface NetworkingService {

    void sendClientbound(ServerPlayer player, CustomPacketPayload payload);
}
