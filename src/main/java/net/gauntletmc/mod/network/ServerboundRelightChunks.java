package net.gauntletmc.mod.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;

public class ServerboundRelightChunks {

    public static final ResourceLocation ID = new ResourceLocation("gauntlet_axiom", "relight_chunks/v2");

    public static void send(Collection<SectionPos> sections) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeCollection(sections, FriendlyByteBuf::writeSectionPos);
        ClientPlayNetworking.send(ID, buf);
    }
}
