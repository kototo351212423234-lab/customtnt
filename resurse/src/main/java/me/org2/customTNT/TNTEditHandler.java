package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TNTEditHandler implements Listener {

    private static final Map<UUID, EditSession> editingSessions = new HashMap<>();

    private static class EditSession {
        String tntType;
        String field;

        EditSession(String tntType, String field) {
            this.tntType = tntType;
            this.field = field;
        }
    }

    public static void startEdit(Player player, String tntType, String field) {
        editingSessions.put(player.getUniqueId(), new EditSession(tntType, field));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!editingSessions.containsKey(player.getUniqueId())) {
            return;
        }

        e.setCancelled(true);

        EditSession session = editingSessions.get(player.getUniqueId());
        editingSessions.remove(player.getUniqueId());

        String input = e.getMessage().trim();

        if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("отмена") || input.equalsIgnoreCase("скасувати")) {
            player.sendMessage(ChatColor.YELLOW + "✘ Редагування скасовано.");
            Bukkit.getScheduler().runTask(CustomTNT.getInstance(), () -> {
                TNTMenuGUI.openEditMenu(player, session.tntType);
            });
            return;
        }

        boolean success = false;
        String fieldPath = "tnt." + session.tntType + "." + session.field;

        try {
            switch (session.field) {
                case "name":
                case "description":
                case "particle-type":
                case "sound-name":
                    CustomTNT.getInstance().getTNTConfig().set(fieldPath, input);
                    success = true;
                    player.sendMessage(ChatColor.GREEN + "✔ " + getFieldDisplayName(session.field) + " змінено на: " +
                            ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', input));
                    break;

                case "radius":
                case "power":
                case "entity-damage-multiplier":
                case "sound-volume":
                case "sound-pitch":
                    double doubleValue = Double.parseDouble(input);
                    if (doubleValue < 0) {
                        player.sendMessage(ChatColor.RED + "✘ Значення не може бути від'ємним!");
                        break;
                    }
                    if (session.field.equals("radius") && doubleValue > 100) {
                        player.sendMessage(ChatColor.RED + "✘ Радіус не може перевищувати 100!");
                        break;
                    }
                    if (session.field.equals("power") && doubleValue > 100) {
                        player.sendMessage(ChatColor.RED + "✘ Сила не може перевищувати 100!");
                        break;
                    }
                    CustomTNT.getInstance().getTNTConfig().set(fieldPath, doubleValue);
                    success = true;
                    player.sendMessage(ChatColor.GREEN + "✔ " + getFieldDisplayName(session.field) + " змінено на: " +
                            ChatColor.WHITE + doubleValue);
                    break;

                case "fuse":
                case "particle-count":
                case "max-blocks":
                    int intValue = Integer.parseInt(input);
                    if (session.field.equals("fuse") && intValue < 0) {
                        player.sendMessage(ChatColor.RED + "✘ Час не може бути від'ємним!");
                        break;
                    }
                    if (session.field.equals("particle-count") && (intValue < 0 || intValue > 1000)) {
                        player.sendMessage(ChatColor.RED + "✘ Кількість частинок має бути від 0 до 1000!");
                        break;
                    }
                    CustomTNT.getInstance().getTNTConfig().set(fieldPath, intValue);
                    success = true;
                    if (session.field.equals("fuse")) {
                        player.sendMessage(ChatColor.GREEN + "✔ Час вибуху змінено на: " +
                                ChatColor.WHITE + (intValue / 20.0) + " секунд");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "✔ " + getFieldDisplayName(session.field) + " змінено на: " +
                                ChatColor.WHITE + intValue);
                    }
                    break;

                case "drop-chance":
                    double chanceValue = Double.parseDouble(input);
                    if (chanceValue < 0 || chanceValue > 1) {
                        player.sendMessage(ChatColor.RED + "✘ Шанс має бути від 0.0 до 1.0 (або від 0% до 100%)!");
                        player.sendMessage(ChatColor.YELLOW + "Підказка: 0.5 = 50%, 0.75 = 75%, 1.0 = 100%");
                        break;
                    }
                    CustomTNT.getInstance().getTNTConfig().set(fieldPath, chanceValue);
                    success = true;
                    player.sendMessage(ChatColor.GREEN + "✔ Шанс випадання змінено на: " +
                            ChatColor.WHITE + (int)(chanceValue * 100) + "%");
                    break;

                default:
                    player.sendMessage(ChatColor.RED + "✘ Невідоме поле: " + session.field);
                    break;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "✘ Невірний формат числа! Спробуйте ще раз.");
            player.sendMessage(ChatColor.YELLOW + "Приклад: 5.0 або 10");
            Bukkit.getScheduler().runTask(CustomTNT.getInstance(), () -> {
                TNTMenuGUI.openEditMenu(player, session.tntType);
            });
            return;
        }

        if (success) {
            CustomTNT.getInstance().saveTNTConfig();
        }

        Bukkit.getScheduler().runTask(CustomTNT.getInstance(), () -> {
            TNTMenuGUI.openEditMenu(player, session.tntType);
        });
    }

    private String getFieldDisplayName(String field) {
        switch (field) {
            case "name": return "Назва";
            case "description": return "Опис";
            case "radius": return "Радіус";
            case "power": return "Сила";
            case "fuse": return "Час вибуху";
            case "entity-damage-multiplier": return "Множник шкоди";
            case "drop-chance": return "Шанс випадання";
            case "max-blocks": return "Ліміт блоків";
            case "particle-type": return "Тип частинок";
            case "particle-count": return "Кількість частинок";
            case "sound-name": return "Назва звуку";
            case "sound-volume": return "Гучність звуку";
            case "sound-pitch": return "Висота звуку";
            default: return field;
        }
    }
}