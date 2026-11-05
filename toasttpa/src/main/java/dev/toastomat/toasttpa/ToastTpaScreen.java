package dev.toastomat.toasttpa;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class ToastTpaScreen extends Screen {
    private static final int ACCENT = 0xFFFF8A32;
    private static final int PANEL = 0xF10D111A;
    private static final int INNER = 0xFF151B27;
    private static final int CARD = 0xFF1C2635;
    private static final int TEXT = 0xFFF2F5FA;
    private static final int MUTED = 0xFF9BA9BC;

    private static final int ROW_HEIGHT = 22;
    private static final int ROW_GAP = 1;

    private TextFieldWidget playerNameField;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int rowsTop;
    private int pageY;
    private int visibleRows;
    private int page;

    public ToastTpaScreen() {
        super(Text.literal("ToastTPA"));
    }

    @Override
    protected void init() {
        // Keep the UI inside the actual scaled Minecraft window.
        panelWidth = Math.max(300, Math.min(560, this.width - 12));
        panelHeight = Math.max(190, Math.min(250, this.height - 8));
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        rowsTop = 110;
        pageY = panelHeight - 55;
        visibleRows = Math.max(1, Math.min(8,
                (pageY - rowsTop - 5) / (ROW_HEIGHT + ROW_GAP)));

        int fieldWidth = Math.max(120, panelWidth - 154);
        playerNameField = new TextFieldWidget(
                this.textRenderer, panelX + 18, panelY + 68, fieldWidth, 22,
                Text.literal("Minecraft-Spielername")
        );
        playerNameField.setMaxLength(16);
        playerNameField.setTextPredicate(value -> value.matches("[A-Za-z0-9_]{0,16}"));
        playerNameField.setPlaceholder(Text.literal("Spielername"));
        addDrawableChild(playerNameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ HINZUFÜGEN"), button -> addFriend())
                .dimensions(panelX + panelWidth - 128, panelY + 68, 110, 22).build());

        rebuildFriendRows();

        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), button -> changePage(-1))
                .dimensions(panelX + 18, panelY + pageY, 28, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), button -> changePage(1))
                .dimensions(panelX + 52, panelY + pageY, 28, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("SCHNELL-TPA"), button -> sendSelectedFriend())
                .dimensions(panelX + panelWidth - 224, panelY + panelHeight - 30, 108, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("SCHLIESSEN"), button -> closeScreen())
                .dimensions(panelX + panelWidth - 108, panelY + panelHeight - 30, 90, 22).build());
    }

    private void rebuildFriendRows() {
        List<String> friends = ToastTpaConfig.getFriends();
        int start = page * visibleRows;
        int end = Math.min(start + visibleRows, friends.size());

        for (int index = start; index < end; index++) {
            String friend = friends.get(index);
            int y = panelY + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(friend.equalsIgnoreCase(ToastTpaConfig.getSelectedFriend())
                            ? "★ AUSGEWÄHLT" : "AUSWÄHLEN"),
                    button -> {
                        ToastTpaConfig.selectFriend(friend);
                        refreshWidgets();
                    })
                    .dimensions(panelX + panelWidth - 224, y, 104, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("TPA"), button -> {
                ToastTpaConfig.selectFriend(friend);
                ToastTpaClient.sendTpa(MinecraftClient.getInstance(), friend);
            }).dimensions(panelX + panelWidth - 112, y, 54, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> {
                ToastTpaConfig.removeFriend(friend);
                page = Math.min(page, maxPage());
                refreshWidgets();
            }).dimensions(panelX + panelWidth - 50, y, 32, 20).build());
        }
    }

    private void addFriend() {
        String name = playerNameField.getText().trim();
        if (ToastTpaConfig.addFriend(name)) {
            page = maxPage();
            ToastTpaClient.notifyPlayer(MinecraftClient.getInstance(), name + " gespeichert.");
            refreshWidgets();
        } else {
            ToastTpaClient.notifyPlayer(MinecraftClient.getInstance(),
                    "Name ungültig, bereits gespeichert oder Freundeliste voll.");
        }
    }

    private void sendSelectedFriend() {
        String selected = ToastTpaConfig.getSelectedFriend();
        if (selected == null || selected.isBlank()) {
            ToastTpaClient.notifyPlayer(MinecraftClient.getInstance(), "Wähle zuerst einen Freund aus.");
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

    /**
     * Screen.render() calls this once before drawing child widgets.
     * Do not invoke the vanilla blurred background: on 1.21.11 the
     * old implementation called it here and then called it a second time
     * manually from render(), causing "Can only blur once per frame".
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Intentionally empty. The dim overlay and panel are drawn in render().
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // No vanilla blur. A simple translucent overlay is stable with Sodium
        // and other screen/rendering mods.
        context.fill(0, 0, this.width, this.height, 0x99000000);

        int x = panelX;
        int y = panelY;

        context.fill(x - 1, y - 1, x + panelWidth + 1, y + panelHeight + 1, 0xAAFF8A32);
        context.fill(x, y, x + panelWidth, y + panelHeight, PANEL);
        context.fill(x + 2, y + 2, x + panelWidth - 2, y + 38, 0xFF202A39);
        context.fill(x + 14, y + 41, x + panelWidth - 14, y + 64, CARD);
        context.fill(x + 14, y + 66, x + panelWidth - 14, y + 93, INNER);
        context.fill(x + 14, y + 105, x + panelWidth - 14, y + pageY - 5, INNER);

        // The overridden renderBackground() prevents Screen.render() from
        // invoking the blur a second time. It still draws all child widgets.
        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(textRenderer, Text.literal("TOASTTPA"), x + 18, y + 10, ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("FRIENDS  /  QUICK TPA"),
                x + 18, y + 24, TEXT);

        String selected = ToastTpaConfig.getSelectedFriend();
        String selectedLabel = selected == null || selected.isBlank() ? "Kein Freund ausgewählt" : selected;
        context.drawTextWithShadow(textRenderer, Text.literal("QUICK TARGET: " + selectedLabel),
                x + 22, y + 49, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("Ä = TPA"),
                x + panelWidth - 88, y + 49, ACCENT);

        context.drawTextWithShadow(textRenderer, Text.literal("FREUND HINZUFÜGEN"),
                x + 18, y + 57, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("DEINE FREUNDE"),
                x + 18, y + 96, ACCENT);

        List<String> friends = ToastTpaConfig.getFriends();
        if (friends.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Noch keine Freunde gespeichert."),
                    x + panelWidth / 2, y + rowsTop + 8, MUTED);
        } else {
            int start = page * visibleRows;
            int end = Math.min(start + visibleRows, friends.size());
            for (int index = start; index < end; index++) {
                String friend = friends.get(index);
                int rowY = y + rowsTop + (index - start) * (ROW_HEIGHT + ROW_GAP);
                boolean isSelected = friend.equalsIgnoreCase(selected);
                context.fill(x + 18, rowY, x + panelWidth - 232, rowY + 20,
                        isSelected ? 0xFF293C43 : CARD);
                context.fill(x + 18, rowY, x + 21, rowY + 20,
                        isSelected ? ACCENT : 0xFF354457);
                context.drawTextWithShadow(textRenderer, Text.literal(friend),
                        x + 28, rowY + 6, TEXT);
            }
        }

        context.drawTextWithShadow(textRenderer,
                Text.literal("Seite " + (page + 1) + " / " + (maxPage() + 1)
                        + "  •  " + friends.size() + "/" + ToastTpaConfig.MAX_FRIENDS),
                x + 88, y + pageY + 6, MUTED);

        context.drawTextWithShadow(textRenderer,
                Text.literal("RECHTS-SHIFT = MENÜ   •   Ä = SCHNELL-TPA"),
                x + 18, y + panelHeight - 14, MUTED);
    }
}
