package dev.toastomat.toasttpa;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class ToastTpaScreen extends Screen {
    private static final int ACCENT = 0xFFFF913D;
    private static final int PANEL = 0xF10D121B;
    private static final int HEADER = 0xFF192231;
    private static final int SURFACE = 0xFF141C28;
    private static final int ROW = 0xFF202B3A;
    private static final int ROW_ACTIVE = 0xFF263A3B;
    private static final int TEXT = 0xFFF3F5F8;
    private static final int MUTED = 0xFF95A3B5;

    private static final int ROW_HEIGHT = 22;
    private static final int ROW_GAP = 2;

    private TextFieldWidget playerNameField;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int rowsTop;
    private int footerY;
    private int visibleRows;
    private int page;

    public ToastTpaScreen() {
        super(Text.literal("ToastTPA"));
    }

    @Override
    protected void init() {
        panelWidth = Math.max(300, Math.min(560, this.width - 12));
        panelHeight = Math.max(160, Math.min(260, this.height - 12));
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        rowsTop = 112;
        footerY = panelHeight - 34;
        visibleRows = Math.max(1, Math.min(5,
                (footerY - rowsTop - 6) / (ROW_HEIGHT + ROW_GAP)));

        int addButtonWidth = 100;
        int fieldWidth = Math.max(110, panelWidth - 154);
        playerNameField = new TextFieldWidget(
                this.textRenderer,
                panelX + 18,
                panelY + 72,
                fieldWidth,
                22,
                Text.translatable("toasttpa.add_placeholder")
        );
        playerNameField.setMaxLength(16);
        playerNameField.setTextPredicate(value -> value.matches("[A-Za-z0-9_]{0,16}"));
        playerNameField.setPlaceholder(Text.translatable("toasttpa.add_placeholder"));
        playerNameField.setDrawsBackground(false);
        addDrawableChild(playerNameField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.add"), button -> addFriend())
                .dimensions(panelX + panelWidth - addButtonWidth - 18, panelY + 72, addButtonWidth, 22)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.quick_tpa"), button -> sendSelectedFriend())
                .dimensions(panelX + panelWidth - 112, panelY + 43, 94, 22)
                .build());

        rebuildFriendRows();

        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), button -> changePage(-1))
                .dimensions(panelX + 18, panelY + footerY, 26, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), button -> changePage(1))
                .dimensions(panelX + 50, panelY + footerY, 26, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.close"), button -> closeScreen())
                .dimensions(panelX + panelWidth - 108, panelY + footerY, 90, 20)
                .build());
    }

    private void rebuildFriendRows() {
        List<String> friends = ToastTpaConfig.getFriends();
        int start = page * visibleRows;
        int end = Math.min(start + visibleRows, friends.size());

        for (int index = start; index < end; index++) {
            String friend = friends.get(index);
            int y = panelY + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);

            addDrawableChild(ButtonWidget.builder(
                    Text.translatable(friend.equalsIgnoreCase(ToastTpaConfig.getSelectedFriend())
                            ? "toasttpa.active" : "toasttpa.select"),
                    button -> {
                        ToastTpaConfig.selectFriend(friend);
                        refreshWidgets();
                    })
                    .dimensions(panelX + panelWidth - 220, y, 94, 20)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.tpa"), button -> {
                ToastTpaConfig.selectFriend(friend);
                ToastTpaClient.sendTpa(MinecraftClient.getInstance(), friend);
            }).dimensions(panelX + panelWidth - 120, y, 48, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> {
                ToastTpaConfig.removeFriend(friend);
                page = Math.min(page, maxPage());
                refreshWidgets();
            }).dimensions(panelX + panelWidth - 64, y, 46, 20).build());
        }
    }

    private void addFriend() {
        String name = playerNameField.getText().trim();
        if (ToastTpaConfig.addFriend(name)) {
            page = maxPage();
            ToastTpaClient.notifyPlayer(Text.translatable("toasttpa.message.saved", name));
            refreshWidgets();
        } else {
            ToastTpaClient.notifyPlayer(Text.translatable("toasttpa.message.invalid"));
        }
    }

    private void sendSelectedFriend() {
        String selected = ToastTpaConfig.getSelectedFriend();
        if (selected == null || selected.isBlank()) {
            ToastTpaClient.notifyPlayer(Text.translatable("toasttpa.message.no_friend"));
            return;
        }
        ToastTpaClient.sendTpa(MinecraftClient.getInstance(), selected);
    }

    private void changePage(int amount) {
        page = Math.max(0, Math.min(maxPage(), page + amount));
        refreshWidgets();
    }

    private int maxPage() {
        return Math.max(0, (ToastTpaConfig.getFriends().size() - 1) / visibleRows);
    }

    private void refreshWidgets() {
        clearAndInit();
    }

    private void closeScreen() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // A custom dim layer is drawn below. Avoid Minecraft's blur pipeline:
        // calling it twice in one frame crashes on Minecraft 1.21.11.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x99070A10);

        int x = panelX;
        int y = panelY;
        int right = x + panelWidth;
        int bottom = y + panelHeight;

        context.fill(x - 1, y - 1, right + 1, bottom + 1, 0xAFFF913D);
        context.fill(x, y, right, bottom, PANEL);
        context.fill(x + 1, y + 1, right - 1, y + 37, HEADER);
        context.fill(x + 14, y + 40, right - 14, y + 68, SURFACE);
        context.fill(x + 14, y + 70, right - 14, y + 98, SURFACE);
        context.fill(x + 14, y + 105, right - 14, y + footerY - 5, SURFACE);

        List<String> friends = ToastTpaConfig.getFriends();
        String selected = ToastTpaConfig.getSelectedFriend();
        int start = page * visibleRows;
        int end = Math.min(start + visibleRows, friends.size());

        for (int index = start; index < end; index++) {
            String friend = friends.get(index);
            int rowY = y + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);
            boolean active = friend.equalsIgnoreCase(selected);
            context.fill(x + 18, rowY, right - 18, rowY + ROW_HEIGHT,
                    active ? ROW_ACTIVE : ROW);
            context.fill(x + 18, rowY, x + 21, rowY + ROW_HEIGHT,
                    active ? ACCENT : 0xFF354457);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(textRenderer, Text.literal("TOASTTPA"), x + 18, y + 9, ACCENT);
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.subtitle"),
                x + 18, y + 23, MUTED);

        Text target = selected == null || selected.isBlank()
                ? Text.translatable("toasttpa.no_target")
                : Text.literal(selected);
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.target"),
                x + 22, y + 47, MUTED);
        context.drawTextWithShadow(textRenderer, target,
                x + 22, y + 57, TEXT);
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.friends"),
                x + 18, y + 100, ACCENT);

        if (friends.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("toasttpa.empty"),
                    x + panelWidth / 2, y + rowsTop + 8, MUTED);
        } else {
            for (int index = start; index < end; index++) {
                String friend = friends.get(index);
                int rowY = y + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);
                context.drawTextWithShadow(textRenderer, Text.literal(friend),
                        x + 29, rowY + 7, TEXT);
            }
        }

        context.drawTextWithShadow(textRenderer,
                Text.translatable("toasttpa.page", page + 1, maxPage() + 1),
                x + 86, y + footerY + 6, MUTED);
        context.drawTextWithShadow(textRenderer,
                Text.translatable("toasttpa.hotkeys"),
                x + 18, bottom - 11, MUTED);
    }
}
