package net.gauntletmc.mod.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ServerboundSetTextScreen {

    public static final ResourceLocation ID = new ResourceLocation("gauntletnotes", "set_text");

    public static void send(List<String> content) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeCollection(content, FriendlyByteBuf::writeUtf);
        ClientPlayNetworking.send(ID, buf);
    }
}
