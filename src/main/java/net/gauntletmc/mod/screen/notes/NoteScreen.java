package net.gauntletmc.mod.screen.notes;

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen;
import net.gauntletmc.mod.network.ClientboundOpenScreen;
import net.gauntletmc.mod.network.ServerboundRequestScreen;
import net.gauntletmc.mod.network.ServerboundSetTextScreen;
import net.gauntletmc.mod.screen.editor.TextEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NoteScreen extends BaseCursorScreen {

    public static final ResourceLocation TEXTURE = new ResourceLocation("gauntletbuild", "textures/gui/heading.png");

    private final UUID id;
    private final List<ClientboundOpenScreen.Entry> entries;
    private final List<String> lines;

    private TextEditor editor;

    public NoteScreen(UUID id, List<ClientboundOpenScreen.Entry> entries, List<String> lines) {
        super(CommonComponents.EMPTY);
        this.id = id;
        this.entries = entries;
        this.lines = lines;
    }

    @Override
    protected void init() {
        int sidebar = (int) (this.width * 0.25f) - 2;
        editor = addRenderableWidget(new TextEditor(
                sidebar + 2, 15, this.width - sidebar + 2, this.height - 15,
                0x000000,
                0x000000,
                Minecraft.getInstance().font,
                Component::literal
        ));
        editor.setContent(String.join("\n", lines));

        if (!Objects.equals(this.id, Minecraft.getInstance().getUser().getProfileId())) {
            editor.canEdit = false;
        }

        NotesList list = addRenderableWidget(new NotesList(0, 15, sidebar, this.height - 15, user -> {
            if (user != null && !user.equals(this.id)) {
                ServerboundRequestScreen.send(user);
            }
        }));
        list.update(this.id, this.entries);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        graphics.fill(0, 0, width, height, 0xD0000000);
        graphics.blitRepeating(TEXTURE, 0, 0, this.width, 15, 0, 0, 128, 15);
        int sidebar = (int) (this.width * 0.25f) - 2;
        graphics.blitRepeating(TEXTURE, sidebar, 15, 2, this.height - 15, 243, 0, 2, 256);
        graphics.blitRepeating(TEXTURE,
                0, 15,
                sidebar, this.height - 15,
                0, 15,
                122, 241
        );
        graphics.blitRepeating(TEXTURE,
                sidebar + 2, 15,
                this.width - sidebar, this.height - 15,
                122, 15,
                121, 241
        );

        String username = this.entries.stream()
                .filter(entry -> entry.id().equals(this.id))
                .map(ClientboundOpenScreen.Entry::username)
                .findFirst()
                .orElse(this.id.equals(Minecraft.getInstance().getUser().getProfileId()) ? Minecraft.getInstance().getUser().getName() : "Unknown");

        int textX = (int) (this.width * 0.25f) + ((int) (this.width * 0.75f) / 2) - font.width("Notes - " + username) / 2;
        graphics.drawString(
                font,
                "Notes - " + username, textX, 3, 0x404040,
                false
        );

        super.render(graphics, i, j, f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        if (Objects.equals(this.id, Minecraft.getInstance().getUser().getProfileId())) {
            String ogText = String.join("\n", lines);
            String newText = String.join("\n", editor.lines());
            if (!ogText.equals(newText)) {
                ServerboundSetTextScreen.send(editor.lines());
            }
        }
    }
}
