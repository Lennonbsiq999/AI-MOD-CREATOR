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

        // On a German QWERTZ keyboard, this physical key is Ä.
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
                if (client.currentScreen == null) client.setScreen(new ToastTpaScreen());
            }

            while (quickTpaKey.wasPressed()) {
                if (client.currentScreen != null) continue;

                String target = ToastTpaConfig.getSelectedFriend();
                if (target == null || target.isBlank()) {
                    client.setScreen(new ToastTpaScreen());
                    notifyPlayer(client, "Wähle zuerst einen Freund im ToastTPA-Menü aus.");
                } else {
                    sendTpa(client, target);
                }
            }
        });
    }

    public static void sendTpa(MinecraftClient client, String playerName) {
        if (!ToastTpaConfig.isValidPlayerName(playerName)) {
            notifyPlayer(client, "Ungültiger Spielername.");
            return;
        }
        if (client.getNetworkHandler() == null || client.player == null) {
            notifyPlayer(client, "Du bist gerade auf keinem Server.");
            return;
        }

        // Sends the normal server command; the other player must accept.
        client.getNetworkHandler().sendChatCommand("tpa " + playerName);
        notifyPlayer(client, "TPA-Anfrage an " + playerName + " gesendet.");
    }

    public static void notifyPlayer(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[ToastTPA] §f" + message), false);
        }
    }
}
