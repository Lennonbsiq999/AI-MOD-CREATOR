package dev.toastomat.toasttpa;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class ToastTpaScreen extends Screen {
    private static final int ACCENT = 0xFFFF9142;
    private static final int PANEL = 0xF10D121B;
    private static final int HEADER = 0xFF192331;
    private static final int SURFACE = 0xFF151E2B;
    private static final int ROW = 0xFF202B39;
    private static final int ROW_ACTIVE = 0xFF263A3A;
    private static final int TEXT = 0xFFF1F4F8;
    private static final int MUTED = 0xFF95A3B5;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 3;

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
        panelWidth = Math.max(300, Math.min(540, this.width - 16));
        panelHeight = Math.max(180, Math.min(270, this.height - 12));
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        rowsTop = 128;
        footerY = panelHeight - 34;
        visibleRows = Math.max(1, Math.min(5,
                (footerY - rowsTop - 6) / (ROW_HEIGHT + ROW_GAP)));

        int addButtonWidth = 92;
        int fieldWidth = Math.max(110, panelWidth - addButtonWidth - 54);
        playerNameField = new TextFieldWidget(
                this.textRenderer,
                panelX + 18,
                panelY + 88,
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
                .dimensions(panelX + panelWidth - addButtonWidth - 18, panelY + 88,
                        addButtonWidth, 22)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.quick_tpa"), button -> sendSelectedFriend())
                .dimensions(panelX + panelWidth - 110, panelY + 45, 92, 22)
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
            int right = panelX + panelWidth - 18;

            int removeX = right - 24;
            int tpaX = removeX - 6 - 44;
            int selectX = tpaX - 6 - 76;

            addDrawableChild(ButtonWidget.builder(
                    Text.translatable(friend.equalsIgnoreCase(ToastTpaConfig.getSelectedFriend())
                            ? "toasttpa.active" : "toasttpa.select"),
                    button -> {
                        ToastTpaConfig.selectFriend(friend);
                        refreshWidgets();
                    })
                    .dimensions(selectX, y, 76, ROW_HEIGHT)
                    .build());

            addDrawableChild(ButtonWidget.builder(Text.translatable("toasttpa.tpa"), button -> {
                ToastTpaConfig.selectFriend(friend);
                ToastTpaClient.sendTpa(MinecraftClient.getInstance(), friend);
            }).dimensions(tpaX, y, 44, ROW_HEIGHT).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> {
                ToastTpaConfig.removeFriend(friend);
                page = Math.min(page, maxPage());
                refreshWidgets();
            }).dimensions(removeX, y, 24, ROW_HEIGHT).build());
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
        // Deliberately skip Minecraft's blur pipeline. It may only be applied
        // once per frame on 1.21.11; the screen uses a lightweight dim overlay.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x99070A10);

        int x = panelX;
        int y = panelY;
        int right = x + panelWidth;
        int bottom = y + panelHeight;

        // Main panel and three calm surface areas.
        context.fill(x - 1, y - 1, right + 1, bottom + 1, 0xAAFF9142);
        context.fill(x, y, right, bottom, PANEL);
        context.fill(x + 1, y + 1, right - 1, y + 38, HEADER);
        context.fill(x + 14, y + 42, right - 14, y + 72, SURFACE);
        context.fill(x + 14, y + 84, right - 14, y + 114, SURFACE);
        context.fill(x + 14, y + 122, right - 14, y + footerY - 5, SURFACE);

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

        // Header.
        context.drawTextWithShadow(textRenderer, Text.literal("ToastTPA"), x + 18, y + 9, ACCENT);
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.subtitle"),
                x + 18, y + 23, MUTED);

        // Selected friend.
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.target"),
                x + 22, y + 47, MUTED);
        String selectedName = selected == null || selected.isBlank()
                ? textRenderer.trimToWidth(Text.translatable("toasttpa.no_target"), panelWidth - 150).getString()
                : selected;
        context.drawTextWithShadow(textRenderer, Text.literal(selectedName),
                x + 22, y + 58, TEXT);

        // Add friend section.
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.add_section"),
                x + 18, y + 76, MUTED);

        // Friend list heading and count.
        context.drawTextWithShadow(textRenderer, Text.translatable("toasttpa.friends"),
                x + 18, y + 116, ACCENT);
        context.drawTextWithShadow(textRenderer,
                Text.literal(friends.size() + " / " + ToastTpaConfig.MAX_FRIENDS),
                right - 64, y + 116, MUTED);

        if (friends.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("toasttpa.empty"),
                    x + panelWidth / 2, y + rowsTop + 7, MUTED);
        } else {
            for (int index = start; index < end; index++) {
                String friend = friends.get(index);
                int rowY = y + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);
                int nameRight = right - 18 - 24 - 6 - 44 - 6 - 76 - 10;
                int nameWidth = Math.max(30, nameRight - (x + 28));
                Text displayName = textRenderer.trimToWidth(Text.literal(friend), nameWidth);
                context.drawTextWithShadow(textRenderer, displayName, x + 29, rowY + 6, TEXT);
            }
        }

        // Footer.
        context.drawTextWithShadow(textRenderer,
                Text.translatable("toasttpa.page", page + 1, maxPage() + 1),
                x + 86, y + footerY + 6, MUTED);
        context.drawTextWithShadow(textRenderer,
                Text.translatable("toasttpa.hotkeys"),
                x + 18, bottom - 12, MUTED);
    }
}
