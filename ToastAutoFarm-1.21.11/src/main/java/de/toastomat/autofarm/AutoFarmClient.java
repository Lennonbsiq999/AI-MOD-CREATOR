package de.toastomat.autofarm;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class AutoFarmClient implements ClientModInitializer {
    private static KeyMapping openMenuKey;
    private static KeyMapping toggleKey;

    private static Block selectedBlock = Blocks.DIAMOND_ORE;
    private static boolean enabled = false;
    private static BlockPos currentTarget = null;
    private static BlockPos lastStartedTarget = null;
    private static int scanCooldown = 0;

    private static final int SEARCH_RADIUS = 5;
    private static final double MAX_DISTANCE_SQUARED = SEARCH_RADIUS * SEARCH_RADIUS;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.toastautofarm.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyMapping.Category.MISC
        ));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.toastautofarm.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(AutoFarmClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (openMenuKey.consumeClick()) {
            client.setScreen(new AutoFarmScreen());
        }
        while (toggleKey.consumeClick()) {
            toggle(client);
        }

        if (!enabled || client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        if (scanCooldown-- <= 0 || !isStillTarget(client, currentTarget)) {
            currentTarget = findNearestVisibleBlock(client);
            scanCooldown = 4;
            if (currentTarget == null) {
                lastStartedTarget = null;
            }
        }

        if (currentTarget == null) {
            client.gameMode.stopDestroyBlock();
            return;
        }

        aimAt(client, currentTarget);
        if (!currentTarget.equals(lastStartedTarget)) {
            client.gameMode.startDestroyBlock(currentTarget, Direction.UP);
            lastStartedTarget = currentTarget;
        } else {
            client.gameMode.continueDestroyBlock(currentTarget, Direction.UP);
            client.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static boolean isStillTarget(Minecraft client, BlockPos pos) {
        if (pos == null || client.level == null) return false;
        BlockState state = client.level.getBlockState(pos);
        return state.is(selectedBlock);
    }

    private static BlockPos findNearestVisibleBlock(Minecraft client) {
        BlockPos origin = client.player.blockPosition();
        Vec3 eye = client.player.getEyePosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    Vec3 center = Vec3.atCenterOf(pos);
                    double distance = eye.distanceToSqr(center);
                    if (distance > MAX_DISTANCE_SQUARED || distance >= bestDistance) continue;
                    if (!client.level.getBlockState(pos).is(selectedBlock)) continue;

                    HitResult hit = client.level.clip(new ClipContext(
                            eye,
                            center,
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            client.player
                    ));
                    if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
                        best = pos.immutable();
                        bestDistance = distance;
                    }
                }
            }
        }
        return best;
    }

    private static void aimAt(Minecraft client, BlockPos pos) {
        Vec3 eye = client.player.getEyePosition();
        Vec3 target = Vec3.atCenterOf(pos);
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-Math.atan2(dy, horizontal) * 180.0 / Math.PI);
        client.player.setYRot(yaw);
        client.player.setXRot(pitch);
    }

    public static void setSelectedBlock(Block block) {
        selectedBlock = block;
        currentTarget = null;
        lastStartedTarget = null;
    }

    public static Block getSelectedBlock() {
        return selectedBlock;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle(Minecraft client) {
        enabled = !enabled;
        if (!enabled) {
            currentTarget = null;
            lastStartedTarget = null;
            if (client.gameMode != null) client.gameMode.stopDestroyBlock();
        }
    }
}
