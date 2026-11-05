package dev.toastomat.toasttpa;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class ToastTpaScreen extends Screen {
    private static final int PANEL_WIDTH = 570;
    private static final int PANEL_HEIGHT = 430;
    private static final int ROWS_PER_PAGE = 5;

    private static final int ACCENT = 0xFFFF8A32;
    private static final int PANEL = 0xF10D111A;
    private static final int PANEL_INNER = 0xFF151B27;
    private static final int CARD = 0xFF1C2635;
    private static final int TEXT = 0xFFF2F5FA;
    private static final int MUTED = 0xFF9BA9BC;

    private TextFieldWidget playerNameField;
    private int panelX;
    private int panelY;
    private int page;

    public ToastTpaScreen() {
        super(Text.literal("ToastTPA"));
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - PANEL_HEIGHT) / 2;

        playerNameField = new TextFieldWidget(
                this.textRenderer, panelX + 24, panelY + 132, 350, 24,
                Text.literal("Minecraft-Spielername")
        );
        playerNameField.setMaxLength(16);
        playerNameField.setTextPredicate(value -> value.matches("[A-Za-z0-9_]{0,16}"));
        playerNameField.setPlaceholder(Text.literal("z. B. Toastomat"));
        playerNameField.setDrawsBackground(false);
        addDrawableChild(playerNameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ FREUND"), button -> addFriend())
                .dimensions(panelX + 386, panelY + 132, 158, 24).build());

        rebuildFriendRows();

        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), button -> changePage(-1))
                .dimensions(panelX + 24, panelY + 382, 34, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), button -> changePage(1))
                .dimensions(panelX + 64, panelY + 382, 34, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("SCHNELL-TPA"), button -> sendSelectedFriend())
                .dimensions(panelX + 132, panelY + 378, 142, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("TASTEN-HILFE"), button -> showKeybindHelp())
                .dimensions(panelX + 286, panelY + 378, 100, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("SCHLIESSEN"), button -> closeScreen())
                .dimensions(panelX + 398, panelY + 378, 146, 30).build());
    }

    private void rebuildFriendRows() {
        List<String> friends = ToastTpaConfig.getFriends();
        int start = page * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, friends.size());

        for (int index = start; index < end; index++) {
            String friend = friends.get(index);
            int y = panelY + 192 + (index - start) * 34;
            boolean selected = friend.equalsIgnoreCase(ToastTpaConfig.getSelectedFriend());

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(selected ? "★ AUSGEWÄHLT" : "AUSWÄHLEN"),
                    button -> {
                        ToastTpaConfig.selectFriend(friend);
                        refreshWidgets();
                    })
                    .dimensions(panelX + 224, y, 126, 25).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("TPA"), button -> {
                ToastTpaConfig.selectFriend(friend);
                ToastTpaClient.sendTpa(MinecraftClient.getInstance(), friend);
            }).dimensions(panelX + 360, y, 78, 25).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("×"), button -> {
                ToastTpaConfig.removeFriend(friend);
                page = Math.min(page, maxPage());
                refreshWidgets();
            }).dimensions(panelX + 448, y, 48, 25).build());
        }
    }

    private void addFriend() {
        String name = playerNameField.getText().trim();
        if (ToastTpaConfig.addFriend(name)) {
            page = maxPage();
            ToastTpaClient.notifyPlayer(MinecraftClient.getInstance(), name + " wurde gespeichert.");
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
        return Math.max(0, (ToastTpaConfig.getFriends().size() - 1) / ROWS_PER_PAGE);
    }

    private void refreshWidgets() {
        clearAndInit();
    }

    private void showKeybindHelp() {
        ToastTpaClient.notifyPlayer(MinecraftClient.getInstance(),
                "Tasten unter Optionen > Steuerung > Tastenbelegung > ToastTPA ändern.");
    }

    private void closeScreen() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int x = panelX;
        int y = panelY;

        context.fill(x - 2, y - 2, x + PANEL_WIDTH + 2, y + PANEL_HEIGHT + 2, 0xAAFF8A32);
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, PANEL);
        context.fill(x + 2, y + 2, x + PANEL_WIDTH - 2, y + 72, 0xFF202A39);
        context.fill(x + 18, y + 80, x + PANEL_WIDTH - 18, y + 118, CARD);
        context.fill(x + 18, y + 124, x + PANEL_WIDTH - 18, y + 162, PANEL_INNER);
        context.fill(x + 18, y + 176, x + PANEL_WIDTH - 18, y + 366, PANEL_INNER);

        context.fill(x + 18, y + 72, x + PANEL_WIDTH - 18, y + 74, ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("TOASTTPA"), x + 24, y + 18, ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("FRIENDS  /  QUICK TELEPORT"), x + 24, y + 39, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("1.21.11  •  FABRIC CLIENT"),
                x + PANEL_WIDTH - 190, y + 26, MUTED);

        String selected = ToastTpaConfig.getSelectedFriend();
        String selectedLabel = selected == null || selected.isBlank() ? "Kein Freund ausgewählt" : selected;
        context.drawTextWithShadow(textRenderer, Text.literal("QUICK TARGET"), x + 30, y + 89, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal(selectedLabel), x + 30, y + 103, TEXT);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Ä  →  /tpa " + (selectedLabel.startsWith("Kein ") ? "Spielername" : selectedLabel)),
                x + 270, y + 96, ACCENT);

        context.drawTextWithShadow(textRenderer, Text.literal("FREUND HINZUFÜGEN"), x + 24, y + 121, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("DEINE FREUNDE"), x + 24, y + 166, ACCENT);

        List<String> friends = ToastTpaConfig.getFriends();
        if (friends.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Noch keine Freunde gespeichert. Füge oben einen Namen hinzu."),
                    x + PANEL_WIDTH / 2, y + 260, MUTED);
        } else {
            int start = page * ROWS_PER_PAGE;
            int end = Math.min(start + ROWS_PER_PAGE, friends.size());
            for (int index = start; index < end; index++) {
                String friend = friends.get(index);
                int rowY = y + 190 + (index - start) * 34;
                boolean isSelected = friend.equalsIgnoreCase(selected);
                context.fill(x + 24, rowY, x + PANEL_WIDTH - 24, rowY + 29,
                        isSelected ? 0xFF293C43 : CARD);
                context.fill(x + 24, rowY, x + 27, rowY + 29,
                        isSelected ? ACCENT : 0xFF354457);
                context.drawTextWithShadow(textRenderer, Text.literal(friend), x + 36, rowY + 10, TEXT);
                if (isSelected) {
                    context.drawTextWithShadow(textRenderer, Text.literal("QUICK TARGET"),
                            x + 152, rowY + 10, ACCENT);
                }
            }
        }

        context.drawTextWithShadow(textRenderer,
                Text.literal("Seite " + (page + 1) + " / " + (maxPage() + 1)
                        + "   •   Max. " + ToastTpaConfig.MAX_FRIENDS + " Freunde"),
                x + 24, y + 358, MUTED);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Ä = TPA senden     |     RECHTS-SHIFT = Menü     |     ESC = Schließen"),
                x + 24, y + PANEL_HEIGHT - 16, MUTED);

        super.render(context, mouseX, mouseY, delta);
    }
}
