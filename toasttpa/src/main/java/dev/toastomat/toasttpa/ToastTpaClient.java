package dev.toastomat.toasttpa;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ToastTpaClient implements ClientModInitializer {
    public static final String MOD_ID = "toasttpa";

    private static KeyBinding openMenuKey;
    private static KeyBinding quickTpaKey;

    @Override
    public void onInitializeClient() {
        ToastTpaConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

        // This physical key maps to Ä on a German QWERTZ keyboard.
        quickTpaKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.toasttpa.quick_tpa",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_APOSTROPHE,
                category
        ));

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.toasttpa.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ToastTpaScreen());
                }
            }

            while (quickTpaKey.wasPressed()) {
                if (client.currentScreen != null) continue;

                String target = ToastTpaConfig.getSelectedFriend();
                if (target == null || target.isBlank()) {
                    client.setScreen(new ToastTpaScreen());
                    notifyPlayer(Text.translatable("toasttpa.message.no_friend"));
                } else {
                    sendTpa(client, target);
                }
            }
        });
    }

    public static void sendTpa(MinecraftClient client, String playerName) {
        if (!ToastTpaConfig.isValidPlayerName(playerName)) {
            notifyPlayer(Text.translatable("toasttpa.message.invalid_name"));
            return;
        }
        if (client.getNetworkHandler() == null || client.player == null) {
            notifyPlayer(Text.translatable("toasttpa.message.not_connected"));
            return;
        }

        // Sends the normal server command; the other player must accept.
        client.getNetworkHandler().sendChatCommand("tpa " + playerName);
        notifyPlayer(Text.translatable("toasttpa.message.sent", playerName));
    }

    public static void notifyPlayer(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§6[ToastTPA] §f").append(message),
                    false
            );
        }
    }
}
