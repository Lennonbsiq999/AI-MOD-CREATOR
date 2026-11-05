package dev.toastomat.toasttpa;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ToastTpaConfig {
    public static final int MAX_FRIENDS = 20;

    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("toasttpa-friends.txt");

    private static final List<String> FRIENDS = new ArrayList<>();
    private static String selectedFriend = "";

    private ToastTpaConfig() {}

    public static void load() {
        FRIENDS.clear();
        selectedFriend = "";

        if (!Files.exists(CONFIG_FILE)) return;

        try {
            for (String line : Files.readAllLines(CONFIG_FILE, StandardCharsets.UTF_8)) {
                if (line.startsWith("selected=")) {
                    String candidate = line.substring("selected=".length()).trim();
                    if (isValidPlayerName(candidate)) selectedFriend = candidate;
                } else if (line.startsWith("friend=")) {
                    String candidate = line.substring("friend=".length()).trim();
                    if (isValidPlayerName(candidate) && !containsIgnoreCase(candidate)
                            && FRIENDS.size() < MAX_FRIENDS) {
                        FRIENDS.add(candidate);
                    }
                }
            }
        } catch (IOException exception) {
            System.err.println("[ToastTPA] Konnte Konfiguration nicht laden: " + exception.getMessage());
        }

        if (!containsIgnoreCase(selectedFriend)) {
            selectedFriend = FRIENDS.isEmpty() ? "" : FRIENDS.getFirst();
        }
    }

    public static List<String> getFriends() {
        return List.copyOf(FRIENDS);
    }

    public static String getSelectedFriend() {
        return selectedFriend;
    }

    public static boolean addFriend(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (!isValidPlayerName(cleaned) || FRIENDS.size() >= MAX_FRIENDS || containsIgnoreCase(cleaned)) {
            return false;
        }

        FRIENDS.add(cleaned);
        if (selectedFriend.isBlank()) selectedFriend = cleaned;
        save();
        return true;
    }

    public static void selectFriend(String name) {
        if (containsIgnoreCase(name)) {
            selectedFriend = findCanonicalName(name);
            save();
        }
    }

    public static void removeFriend(String name) {
        FRIENDS.removeIf(friend -> friend.equalsIgnoreCase(name));
        if (selectedFriend.equalsIgnoreCase(name)) {
            selectedFriend = FRIENDS.isEmpty() ? "" : FRIENDS.getFirst();
        }
        save();
    }

    public static boolean isValidPlayerName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{1,16}");
    }

    private static boolean containsIgnoreCase(String name) {
        if (name == null || name.isBlank()) return false;
        return FRIENDS.stream().anyMatch(friend ->
                friend.toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)));
    }

    private static String findCanonicalName(String name) {
        return FRIENDS.stream()
                .filter(friend -> friend.equalsIgnoreCase(name))
                .findFirst()
                .orElse(name);
    }

    private static void save() {
        List<String> lines = new ArrayList<>();
        lines.add("selected=" + selectedFriend);
        for (String friend : FRIENDS) lines.add("friend=" + friend);

        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.write(CONFIG_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[ToastTPA] Konnte Konfiguration nicht speichern: " + exception.getMessage());
        }
    }
}
