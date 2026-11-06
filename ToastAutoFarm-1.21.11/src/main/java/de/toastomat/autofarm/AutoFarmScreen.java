package de.toastomat.autofarm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class AutoFarmScreen extends Screen {
    public AutoFarmScreen() {
        super(Component.literal("Toast AutoFarm"));
    }

    @Override
    protected void init() {
        int buttonWidth = 142;
        int buttonHeight = 22;
        int gap = 8;
        int left = this.width / 2 - buttonWidth - gap / 2;
        int right = this.width / 2 + gap / 2;
        int top = this.height / 2 - 62;

        addBlockButton("Diamond Ore", Blocks.DIAMOND_ORE, left, top, buttonWidth, buttonHeight);
        addBlockButton("Deepslate Diamond", Blocks.DEEPSLATE_DIAMOND_ORE, right, top, buttonWidth, buttonHeight);
        addBlockButton("Ancient Debris", Blocks.ANCIENT_DEBRIS, left, top + 28, buttonWidth, buttonHeight);
        addBlockButton("Nether Gold Ore", Blocks.NETHER_GOLD_ORE, right, top + 28, buttonWidth, buttonHeight);
        addBlockButton("Emerald Ore", Blocks.EMERALD_ORE, left, top + 56, buttonWidth, buttonHeight);
        addBlockButton("Iron Ore", Blocks.IRON_ORE, right, top + 56, buttonWidth, buttonHeight);
        addBlockButton("Gold Ore", Blocks.GOLD_ORE, left, top + 84, buttonWidth, buttonHeight);
        addBlockButton("Coal Ore", Blocks.COAL_ORE, right, top + 84, buttonWidth, buttonHeight);

        addRenderableWidget(Button.builder(
                Component.literal(AutoFarmClient.isEnabled() ? "Stop AutoFarm" : "Start AutoFarm"),
                button -> {
                    AutoFarmClient.toggle(Minecraft.getInstance());
                    this.clearWidgets();
                    this.init();
                }
        ).bounds(this.width / 2 - 95, top + 116, 190, 22).build());

        addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> onClose()
        ).bounds(this.width / 2 - 95, top + 144, 190, 22).build());
    }

    private void addBlockButton(String label, Block block, int x, int y, int width, int height) {
        String shownLabel = AutoFarmClient.getSelectedBlock() == block ? "✓ " + label : label;
        addRenderableWidget(Button.builder(Component.literal(shownLabel), button -> {
            AutoFarmClient.setSelectedBlock(block);
            this.clearWidgets();
            this.init();
        }).bounds(x, y, width, height).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 88, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.literal("Block: " + BuiltInRegistries.BLOCK.getKey(AutoFarmClient.getSelectedBlock())),
                this.width / 2, this.height / 2 - 76, 0xA0FFA0);
        graphics.drawCenteredString(this.font,
                Component.literal("Status: " + (AutoFarmClient.isEnabled() ? "ACTIVE" : "OFF") + " | O = Menu | P = Start/Stop"),
                this.width / 2, this.height / 2 + 91, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
