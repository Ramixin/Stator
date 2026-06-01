package net.ramixin.stator.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientNetworkingService {

    void sendServerbound(CustomPacketPayload payload);

}
