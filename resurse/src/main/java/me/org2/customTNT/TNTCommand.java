package me.org2.customTNT;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TNTCommand implements CommandExecutor {

    private final CustomTNT plugin;

    public TNTCommand(CustomTNT plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            if (!sender.hasPermission("customtnt.help")) {
                sender.sendMessage(ChatColor.RED + "У вас немає прав для використання цієї команди!");
                return true;
            }
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                if (!sender.hasPermission("customtnt.help")) {
                    sender.sendMessage(ChatColor.RED + "У вас немає прав для використання цієї команди!");
                    return true;
                }
                sendHelp(sender);
                break;

            case "menu":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Цю команду може використовувати тільки гравець!");
                    return true;
                }
                if (!sender.hasPermission("customtnt.menu")) {
                    sender.sendMessage(ChatColor.RED + "У вас немає прав для використання цієї команди!");
                    return true;
                }
                Player player = (Player) sender;
                TNTMenuGUI.openMainMenu(player);
                break;

            case "editor":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Цю команду може використовувати тільки гравець!");
                    return true;
                }
                if (!sender.hasPermission("customtnt.editor")) {
                    sender.sendMessage(ChatColor.RED + "У вас немає прав для використання редактора!");
                    return true;
                }
                Player editorPlayer = (Player) sender;

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Використання: /tntcam editor <main|edit|craft|reset|reload>");
                    return true;
                }

                handleEditorCommand(editorPlayer, args);
                break;

            case "reload":
                if (!sender.hasPermission("customtnt.reload")) {
                    sender.sendMessage(ChatColor.RED + "У вас немає прав для використання цієї команди!");
                    return true;
                }
                plugin.reloadTNTConfig();
                sender.sendMessage("");
                sender.sendMessage(ChatColor.GREEN + "╔════════════════════════════════╗");
                sender.sendMessage(ChatColor.GREEN + "║  " + ChatColor.GOLD + "CustomTNT успішно перезавантажено!" + ChatColor.GREEN + " ║");
                sender.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "Конфігурації оновлено!       " + ChatColor.GREEN + " ║");
                sender.sendMessage(ChatColor.GREEN + "╚════════════════════════════════╝");
                sender.sendMessage("");
                break;

            case "give":
                if (!sender.hasPermission("customtnt.give")) {
                    sender.sendMessage(ChatColor.RED + "У вас немає прав для використання цієї команди!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Використання: /tntcam give <гравець> <тип_tnt> [кількість]");
                    return true;
                }
                handleGiveCommand(sender, args);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Невідома команда! Використовуйте /tntcam help");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════╗");
        sender.sendMessage(ChatColor.GOLD + "║        " + ChatColor.YELLOW + "CustomTNT Plugin v1.0" + ChatColor.GOLD + "         ║");
        sender.sendMessage(ChatColor.GOLD + "╠════════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "Автор: " + ChatColor.AQUA + "nonentity1732" + ChatColor.GOLD + "                ║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "Дата створення: " + ChatColor.AQUA + "12.10.2025" + ChatColor.GOLD + "    ║");
        sender.sendMessage(ChatColor.GOLD + "╠════════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "Опис:" + ChatColor.GOLD + "                             ║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.GRAY + "Плагін для створення кастомного TNT" + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.GRAY + "з налаштовуваними параметрами вибуху," + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.GRAY + "крафтами та зручним GUI інтерфейсом." + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "╠════════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.GREEN + "Команди:" + ChatColor.GOLD + "                          ║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/tntcam help" + ChatColor.GRAY + " - Показати допомогу " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/tntcam menu" + ChatColor.GRAY + " - Відкрити GUI меню " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/crafttnt" + ChatColor.GRAY + " - Відкрити крафт TNT  " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/tntcam editor" + ChatColor.GRAY + " - Редактор GUI    " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/tntcam reload" + ChatColor.GRAY + " - Перезавантажити " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/tntcam give <гравець> <тип> [к-ть]" + ChatColor.GOLD + "");
        sender.sendMessage(ChatColor.GOLD + "║    " + ChatColor.GRAY + "Видати кастомний TNT гравцю      " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "╠════════════════════════════════════════╣");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.LIGHT_PURPLE + "Права доступу:" + ChatColor.GOLD + "                    ║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.help" + ChatColor.GRAY + "   - Перегляд допомоги" + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.menu" + ChatColor.GRAY + "   - Відкрити меню    " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.craft" + ChatColor.GRAY + "  - Відкрити крафт   " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.editor" + ChatColor.GRAY + " - Редактор GUI     " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.reload" + ChatColor.GRAY + " - Перезавантаження" + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.give" + ChatColor.GRAY + "   - Видати TNT      " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "customtnt.create" + ChatColor.GRAY + " - Створити TNT    " + ChatColor.GOLD + "║");
        sender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════╝");
        sender.sendMessage("");
    }

    private void handleEditorCommand(Player player, String[] args) {
        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "main":
                TNTMenuGUI.openMainMenu(player);
                player.sendMessage(ChatColor.GREEN + "✔ Відкрито головне меню редактора");
                break;

            case "edit":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Використання: /tntcam editor edit <тип_tnt>");
                    return;
                }
                String tntType = args[2];
                if (!plugin.getTNTConfig().contains("tnt." + tntType)) {
                    player.sendMessage(ChatColor.RED + "✘ TNT типу '" + tntType + "' не існує!");
                    return;
                }
                TNTMenuGUI.openEditMenu(player, tntType);
                player.sendMessage(ChatColor.GREEN + "✔ Відкрито меню редагування TNT: " + tntType);
                break;

            case "craft":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Використання: /tntcam editor craft <тип_tnt>");
                    return;
                }
                String craftType = args[2];
                if (!plugin.getTNTConfig().contains("tnt." + craftType)) {
                    player.sendMessage(ChatColor.RED + "✘ TNT типу '" + craftType + "' не існує!");
                    return;
                }
                // Використовуємо екземпляр з плагіна
                TNTCraftEditorGUI editorGUI = plugin.getCraftEditorGUI();
                if (editorGUI != null) {
                    editorGUI.openCraftEditor(player, craftType);
                    player.sendMessage(ChatColor.GREEN + "✔ Відкрито редактор крафту для: " + craftType);
                } else {
                    player.sendMessage(ChatColor.RED + "✘ Помилка: редактор крафту не ініціалізовано!");
                }
                break;

            case "reset":
                if (!player.hasPermission("customtnt.editor.reset")) {
                    player.sendMessage(ChatColor.RED + "У вас немає прав для скидання конфігурації!");
                    return;
                }
                player.sendMessage(ChatColor.YELLOW + "⚠ Ця функція ще не реалізована");
                break;

            case "reload":
                plugin.reloadTNTConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Конфігурацію редактора перезавантажено!");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Невідома підкоманда! Використовуйте:");
                player.sendMessage(ChatColor.YELLOW + "/tntcam editor main - Головне меню");
                player.sendMessage(ChatColor.YELLOW + "/tntcam editor edit <тип> - Редагувати TNT");
                player.sendMessage(ChatColor.YELLOW + "/tntcam editor craft <тип> - Редагувати крафт");
                player.sendMessage(ChatColor.YELLOW + "/tntcam editor reload - Перезавантажити");
                break;
        }
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Гравець не знайдений або не в мережі!");
            return;
        }

        String tntType = args[2];
        int amount = 1;

        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Кількість має бути від 1 до 64!");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Невірна кількість! Використовуйте число від 1 до 64.");
                return;
            }
        }

        if (!plugin.getTNTConfig().contains("tnt." + tntType)) {
            sender.sendMessage(ChatColor.RED + "TNT типу '" + tntType + "' не існує!");
            sender.sendMessage(ChatColor.YELLOW + "Використовуйте /tntcam menu для перегляду доступних типів.");
            return;
        }

        target.getInventory().addItem(TNTManager.createTNTItem(tntType, amount));
        sender.sendMessage(ChatColor.GREEN + "✔ Ви видали " + ChatColor.YELLOW + amount + "x " + tntType + " TNT " + ChatColor.GREEN + "гравцю " + ChatColor.AQUA + target.getName());
        target.sendMessage(ChatColor.GREEN + "✔ Ви отримали " + ChatColor.YELLOW + amount + "x " + tntType + " TNT");
    }
}