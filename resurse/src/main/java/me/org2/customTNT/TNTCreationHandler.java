package me.org2.customTNT;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TNTCreationHandler implements Listener {

    private static final Map<UUID, Boolean> creatingPlayers = new HashMap<>();

    public static void startCreation(Player player) {
        creatingPlayers.put(player.getUniqueId(), true);
    }

    public static boolean isCreating(Player player) {
        return creatingPlayers.containsKey(player.getUniqueId());
    }

    public static void cancelCreation(Player player) {
        creatingPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!creatingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        e.setCancelled(true);
        creatingPlayers.remove(player.getUniqueId());

        String input = e.getMessage().trim();

        if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("отмена")) {
            player.sendMessage(ChatColor.YELLOW + "✘ Создание TNT отменено.");
            return;
        }

        String tntId = input.toLowerCase().replaceAll("[^a-z0-9_]", "");

        if (tntId.isEmpty()) {
            player.sendMessage(ChatColor.RED + "✘ ID не может быть пустым!");
            player.sendMessage(ChatColor.YELLOW + "Используйте только буквы, цифры и подчеркивание.");
            return;
        }

        if (tntId.length() < 3) {
            player.sendMessage(ChatColor.RED + "✘ ID должен содержать минимум 3 символа!");
            return;
        }

        if (tntId.length() > 32) {
            player.sendMessage(ChatColor.RED + "✘ ID не может быть длиннее 32 символов!");
            return;
        }

        // Проверка на существование
        if (CustomTNT.getInstance().getTNTConfig().contains("tnt." + tntId)) {
            player.sendMessage(ChatColor.RED + "✘ TNT с ID '" + tntId + "' уже существует!");
            player.sendMessage(ChatColor.YELLOW + "Выберите другое название или удалите существующий.");
            return;
        }

        String displayName = tntId.toUpperCase().replace("_", " ");
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".name", "&e" + displayName + " TNT");
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".radius", 5.0);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".power", 5.0);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".fuse", 80);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".fire", false);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".damage-blocks", true);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".description", "&7Новое кастомное TNT");
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".break-obsidian", false);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".break-bedrock", false);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".water-proof", false);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".ignore-protection", false);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".damage-entities", true);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".entity-damage-multiplier", 1.0);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".particles", true);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".particle-type", "EXPLOSION_LARGE");
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".particle-count", 50);
        CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId + ".custom-sound", false);
        CustomTNT.getInstance().saveTNTConfig();

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "╔════════════════════════════════════════╗");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.GOLD + "✔ TNT успешно создано!              " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "╠════════════════════════════════════════╣");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "ID: " + ChatColor.WHITE + String.format("%-32s", tntId) + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "Название: " + ChatColor.WHITE + String.format("%-26s", displayName + " TNT") + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.GRAY + "────────────────────────────────────" + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.AQUA + "Параметры по умолчанию:             " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "• Радиус: " + ChatColor.WHITE + "5.0                      " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "• Сила: " + ChatColor.WHITE + "5.0                        " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "• Время: " + ChatColor.WHITE + "4.0 секунды               " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "• Огонь: " + ChatColor.RED + "Нет                       " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "• Урон блокам: " + ChatColor.GREEN + "Да               " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "╠════════════════════════════════════════╣");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.LIGHT_PURPLE + "Открывается меню редактирования...  " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "╚════════════════════════════════════════╝");
        player.sendMessage("");

        CustomTNT.getInstance().getServer().getScheduler().runTask(CustomTNT.getInstance(), () -> {
            TNTMenuGUI.openEditMenu(player, tntId);
        });
    }
}