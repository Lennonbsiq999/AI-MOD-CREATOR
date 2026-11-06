package de.toastomat.autofarm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AutoFarmScreen extends Screen {
    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 294;
    private static final int PAGE_SIZE = 10;
    private static final List<Block> ALL_BLOCKS = createBlockList();

    private EditBox searchBox;
    private String searchText = "";
    private int page = 0;

    public AutoFarmScreen() {
        super(Component.literal("Toast AutoFarm Cleaner"));
    }

    private static List<Block> createBlockList() {
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            blocks.add(block);
        }
        blocks.sort(Comparator.comparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
        return List.copyOf(blocks);
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        searchBox = new EditBox(this.font, left + 16, top + 48, PANEL_WIDTH - 32, 20,
                Component.literal("Block suchen"));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("Name oder minecraft:block_id suchen"));
        searchBox.setResponder(value -> {
            searchText = value.toLowerCase(Locale.ROOT).trim();
            page = 0;
            rebuildControls(true);
        });

        rebuildControls(false);
    }

    private List<Block> getFilteredBlocks() {
        if (searchText.isEmpty()) {
            return ALL_BLOCKS;
        }

        List<Block> filtered = new ArrayList<>();
        for (Block block : ALL_BLOCKS) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            String name = block.getName().getString().toLowerCase(Locale.ROOT);
            String key = id.toString().toLowerCase(Locale.ROOT);
            if (name.contains(searchText) || key.contains(searchText)) {
                filtered.add(block);
            }
        }
        return filtered;
    }

    private void rebuildControls(boolean keepSearchFocus) {
        clearWidgets();

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        addRenderableWidget(searchBox);

        List<Block> filtered = getFilteredBlocks();
        int pageCount = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pageCount - 1));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());

        for (int index = start; index < end; index++) {
            Block block = filtered.get(index);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            String label = block.getName().getString();
            if (label.length() > 23) {
                label = label.substring(0, 20) + "...";
            }
            if (block == AutoFarmClient.getSelectedBlock()) {
                label = "✓ " + label;
            }

            int localIndex = index - start;
            int column = localIndex % 2;
            int row = localIndex / 2;
            int x = left + 16 + column * 174;
            int y = top + 76 + row * 24;
            String buttonLabel = label;

            addRenderableWidget(Button.builder(Component.literal(buttonLabel), button -> {
                AutoFarmClient.setSelectedBlock(block);
                rebuildControls(false);
            }).bounds(x, y, 168, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("‹ Zurück"), button -> {
            page = Math.max(0, page - 1);
            rebuildControls(false);
        }).bounds(left + 82, top + 202, 82, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Weiter ›"), button -> {
            page = Math.min(pageCount - 1, page + 1);
            rebuildControls(false);
        }).bounds(left + 216, top + 202, 82, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Auto-Tool: " + (AutoFarmClient.isAutoToolEnabled() ? "AN" : "AUS")),
                button -> {
                    AutoFarmClient.toggleAutoTool();
                    rebuildControls(false);
                }
        ).bounds(left + 16, top + 230, 168, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal(AutoFarmClient.isEnabled() ? "Farmen: STOPP" : "Farmen: START"),
                button -> {
                    AutoFarmClient.toggle(Minecraft.getInstance());
                    rebuildControls(false);
                }
        ).bounds(left + 196, top + 230, 168, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Schließen"), button -> onClose())
                .bounds(left + 100, top + 260, 180, 20).build());

        if (keepSearchFocus) {
            setFocused(searchBox);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0181E29);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, 0xFF37C6A0);
        graphics.drawCenteredString(this.font, "TOAST AUTOFARM CLEANER",
                this.width / 2, top + 10, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font,
                "Ausgewählt: " + BuiltInRegistries.BLOCK.getKey(AutoFarmClient.getSelectedBlock()),
                this.width / 2, top + 29, 0xFFB7E9D9);

        List<Block> filtered = getFilteredBlocks();
        int pageCount = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        String count = filtered.size() + " Blöcke  •  Seite " + (page + 1) + "/" + pageCount;
        graphics.drawCenteredString(this.font, count, this.width / 2, top + 190, 0xFFB8C0CC);

        graphics.drawCenteredString(this.font,
                "Auto-Tool wählt das beste Werkzeug aus der Hotbar.",
                this.width / 2, top + 256, 0xFF9FAAB9);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
