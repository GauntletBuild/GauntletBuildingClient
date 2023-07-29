package net.gauntletmc.mod.screen.notes;

import com.teamresourceful.resourcefullib.client.components.selection.ListEntry;
import com.teamresourceful.resourcefullib.client.components.selection.SelectionList;
import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack;
import com.teamresourceful.resourcefullib.client.screens.CursorScreen;
import com.teamresourceful.resourcefullib.client.utils.CursorUtils;
import com.teamresourceful.resourcefullib.client.utils.ScreenUtils;
import net.gauntletmc.mod.network.ClientboundOpenScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class NotesList extends SelectionList<NotesList.NotesEntry> {

    public NotesList(int x, int y, int width, int height, Consumer<@Nullable UUID> onSelection) {
        super(x, y, width, height, 13, entry -> onSelection.accept(entry == null ? null : entry.uuid), true);
    }

    public void update(UUID id, List<ClientboundOpenScreen.Entry> users) {
        NotesEntry selectedEntry = null;
        List<NotesEntry> entries = new ArrayList<>();
        boolean hasSelf = false;
        for (ClientboundOpenScreen.Entry user : users) {
            NotesEntry entry = new NotesEntry(user.username(), user.id(), user.date());
            entries.add(entry);
            if (user.id().equals(id)) {
                selectedEntry = entry;
            }
            if (user.id().equals(Minecraft.getInstance().getUser().getProfileId())) {
                hasSelf = true;
            }
        }
        User user = Minecraft.getInstance().getUser();
        if (!hasSelf) {
            entries.add(0, new NotesEntry(user.getName(), user.getProfileId(), new Date()));
            if (id.equals(user.getProfileId())) {
                selectedEntry = entries.get(0);
            }
        }
        updateEntries(entries);
        setSelected(selectedEntry);
    }

    public static class NotesEntry extends ListEntry {

        private final String user;
        private final UUID uuid;
        private final Date date;

        public NotesEntry(String user, UUID uuid, Date date) {
            this.user = user;
            this.uuid = uuid;
            this.date = date;
        }

        @Override
        protected void render(@NotNull GuiGraphics graphics, @NotNull ScissorBoxStack stack, int id, int left, int top, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick, boolean selected) {
            if (hovered) {
                graphics.fill(left, top, left + width, top + height, 0x80FFFFFF);
            } else if (selected) {
                graphics.fill(left, top, left + width, top + height, 0x80808080);
            }
            Font font = Minecraft.getInstance().font;
            graphics.drawString(font, Component.literal(user), left + 5, top + 2, 0x404040, false);
            if (hovered) {
                ScreenUtils.setTooltip(List.of(
                    Component.literal(user),
                    Component.literal("ID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(uuid.toString())),
                    Component.literal("Last Edited: ").withStyle(ChatFormatting.GRAY).append(Component.literal(DateFormat.getDateTimeInstance().format(date)))
                ));
                CursorUtils.setCursor(true, CursorScreen.Cursor.POINTER);
            }
        }

        @Override
        public void setFocused(boolean bl) {}

        @Override
        public boolean isFocused() {
            return false;
        }
    }

}
