package net.gauntletmc.mod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.gauntletmc.mod.screen.notes.NoteScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ClientboundOpenScreen {

    public static final ResourceLocation ID = new ResourceLocation("gauntletnotes", "open_screen");

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                ID,
                (client, handler, buffer, response) -> {
                    UUID id = buffer.readUUID();
                    List<Entry> entries = buffer.readList(Entry::new);
                    List<String> lines = buffer.readList(FriendlyByteBuf::readUtf);
                    client.execute(() -> client.setScreen(new NoteScreen(id, entries, lines)));
                }
        );
    }

    public record Entry(String username, UUID id, Date date) {

        public Entry(FriendlyByteBuf buf) {
            this(buf.readUtf(), buf.readUUID(), buf.readDate());
        }
    }
}
