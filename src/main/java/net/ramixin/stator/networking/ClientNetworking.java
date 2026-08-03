package net.ramixin.stator.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientNetworking {

    void sendServerbound(CustomPacketPayload payload);

}
