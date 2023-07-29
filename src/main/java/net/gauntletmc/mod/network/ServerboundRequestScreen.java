package net.gauntletmc.mod.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ServerboundRequestScreen {

    public static final ResourceLocation ID = new ResourceLocation("gauntletnotes", "request_screen");

    public static void send(UUID uuid) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(uuid);
        ClientPlayNetworking.send(ID, buf);
    }
}
