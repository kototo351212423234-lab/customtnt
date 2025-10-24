package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TNTMenuGUI implements Listener {

    private static final String MAIN_MENU_TITLE = ChatColor.DARK_RED + "☢ CustomTNT Меню ☢";
    private static final String EDIT_MENU_TITLE = ChatColor.GOLD + "✎ Редагувати TNT: ";

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_MENU_TITLE);

        ItemStack createButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "✦ Створити новий TNT ✦");
        List<String> createLore = new ArrayList<>();
        createLore.add(ChatColor.GRAY + "════════════════════");
        createLore.add(ChatColor.YELLOW + "Натисніть, щоб створити");
        createLore.add(ChatColor.YELLOW + "новий тип кастомного TNT");
        createLore.add(ChatColor.GRAY + "════════════════════");
        createMeta.setLore(createLore);
        createButton.setItemMeta(createMeta);
        inv.setItem(4, createButton);

        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "ℹ Інформація");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "════════════════════");
        infoLore.add(ChatColor.YELLOW + "Плагін: " + ChatColor.WHITE + "CustomTNT v1.0");
        infoLore.add(ChatColor.YELLOW + "Автор: " + ChatColor.WHITE + "nonentity1732");
        infoLore.add(ChatColor.GRAY + "════════════════════");
        infoMeta.setLore(infoLore);
        infoButton.setItemMeta(infoMeta);
        inv.setItem(0, infoButton);

        Set<String> tntTypes = CustomTNT.getInstance().getTNTConfig().getConfigurationSection("tnt") != null
                ? CustomTNT.getInstance().getTNTConfig().getConfigurationSection("tnt").getKeys(false)
                : new java.util.HashSet<>();

        int slot = 18;
        for (String tntType : tntTypes) {
            if (slot >= 45) break;

            ItemStack tntItem = new ItemStack(Material.TNT);
            ItemMeta tntMeta = tntItem.getItemMeta();

            String name = CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".name", tntType);
            double radius = CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".radius", 4.0);
            double power = CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".power", 4.0);
            int fuse = CustomTNT.getInstance().getTNTConfig().getInt("tnt." + tntType + ".fuse", 80);
            boolean fire = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".fire", false);
            boolean damageBlocks = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".damage-blocks", true);
            String description = CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".description", "");

            tntMeta.setDisplayName(ChatColor.RED + "☢ " + ChatColor.translateAlternateColorCodes('&', name));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "═══════════════════");
            lore.add(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + tntType);
            if (!description.isEmpty()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', description));
                lore.add("");
            }
            lore.add(ChatColor.YELLOW + "Радіус: " + ChatColor.WHITE + radius);
            lore.add(ChatColor.YELLOW + "Сила: " + ChatColor.WHITE + power);
            lore.add(ChatColor.YELLOW + "Час: " + ChatColor.WHITE + (fuse / 20.0) + "с");
            lore.add(ChatColor.YELLOW + "Вогонь: " + (fire ? ChatColor.GREEN + "Так" : ChatColor.RED + "Ні"));
            lore.add(ChatColor.YELLOW + "Руйнування: " + (damageBlocks ? ChatColor.GREEN + "Так" : ChatColor.RED + "Ні"));
            lore.add(ChatColor.GRAY + "═══════════════════");
            lore.add(ChatColor.AQUA + "▶ ЛКМ - Редагувати");
            lore.add(ChatColor.RED + "▶ ПКМ - Видалити");
            tntMeta.setLore(lore);

            NamespacedKey key = new NamespacedKey(CustomTNT.getInstance(), "tnt_id");
            tntMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tntType);

            tntItem.setItemMeta(tntMeta);
            inv.setItem(slot, tntItem);
            slot++;
        }

        // Декор
        ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 9; i++) {
            if (i != 4 && i != 0) inv.setItem(i, glass);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }

    public static void openEditMenu(Player player, String tntType) {
        Inventory inv = Bukkit.createInventory(null, 54, EDIT_MENU_TITLE + tntType);

        // ========== ОСНОВНІ ПАРАМЕТРИ (Рядок 1) ==========
        ItemStack nameButton = createButton(Material.NAME_TAG,
                ChatColor.YELLOW + "✎ Назва",
                "Поточна: " + ChatColor.translateAlternateColorCodes('&',
                        CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".name")),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(10, nameButton);

        ItemStack radiusButton = createButton(Material.ENDER_PEARL,
                ChatColor.YELLOW + "◉ Радіус вибуху",
                "Поточний: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".radius"),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(11, radiusButton);

        ItemStack powerButton = createButton(Material.BLAZE_POWDER,
                ChatColor.YELLOW + "⚡ Сила вибуху",
                "Поточна: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".power"),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(12, powerButton);

        ItemStack fuseButton = createButton(Material.CLOCK,
                ChatColor.YELLOW + "⏰ Час до вибуху",
                "Поточний: " + ChatColor.WHITE + (CustomTNT.getInstance().getTNTConfig().getInt("tnt." + tntType + ".fuse", 80) / 20.0) + "с",
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(13, fuseButton);

        ItemStack descButton = createButton(Material.PAPER,
                ChatColor.YELLOW + "📝 Опис",
                "Поточний: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".description", "Не встановлено"),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(14, descButton);

        boolean fire = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".fire", false);
        ItemStack fireButton = createToggleButton(Material.FLINT_AND_STEEL,
                ChatColor.YELLOW + "🔥 Створювати вогонь", fire);
        inv.setItem(19, fireButton);

        boolean damageBlocks = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".damage-blocks", true);
        ItemStack damageButton = createToggleButton(Material.DIAMOND_PICKAXE,
                ChatColor.YELLOW + "⛏ Руйнувати блоки", damageBlocks);
        inv.setItem(20, damageButton);

        boolean autoIgnite = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".auto-ignite", true);
        ItemStack autoIgniteButton = createToggleButton(Material.FLINT,
                ChatColor.YELLOW + "🔥 Автопідпалювання", autoIgnite);
        inv.setItem(21, autoIgniteButton);

        boolean waterProof = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".water-proof", false);
        ItemStack waterButton = createToggleButton(Material.WATER_BUCKET,
                ChatColor.YELLOW + "💧 Вибухає під водою", waterProof);
        inv.setItem(22, waterButton);

        boolean breakObsidian = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".break-obsidian", false);
        ItemStack obsidianButton = createToggleButton(Material.OBSIDIAN,
                ChatColor.YELLOW + "🪨 Ломати обсидіан", breakObsidian);
        inv.setItem(23, obsidianButton);

        boolean breakBedrock = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".break-bedrock", false);
        ItemStack bedrockButton = createToggleButton(Material.BEDROCK,
                ChatColor.YELLOW + "⬛ Ломати бедрок", breakBedrock);
        inv.setItem(24, bedrockButton);

        boolean ignoreProtection = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".ignore-protection", false);
        ItemStack protectionButton = createToggleButton(Material.SHIELD,
                ChatColor.YELLOW + "🛡 Ігнорувати захист", ignoreProtection);
        inv.setItem(25, protectionButton);

        boolean damageEntities = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".damage-entities", true);
        ItemStack entitiesButton = createToggleButton(Material.IRON_SWORD,
                ChatColor.YELLOW + "⚔ Шкода сутностям", damageEntities);
        inv.setItem(28, entitiesButton);

        ItemStack multiplierButton = createButton(Material.DIAMOND_SWORD,
                ChatColor.YELLOW + "💪 Множник шкоди",
                "Поточний: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".entity-damage-multiplier", 1.0) + "x",
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(29, multiplierButton);

        boolean dropItems = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".drop-items", false);
        ItemStack dropButton = createToggleButton(Material.CHEST,
                ChatColor.YELLOW + "📦 Випадання блоків", dropItems);
        inv.setItem(30, dropButton);

        ItemStack dropChanceButton = createButton(Material.HOPPER,
                ChatColor.YELLOW + "🎲 Шанс випадання",
                "Поточний: " + ChatColor.WHITE + (int)(CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".drop-chance", 0.3) * 100) + "%",
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(31, dropChanceButton);

        ItemStack limitButton = createButton(Material.BARRIER,
                ChatColor.YELLOW + "🧱 Ліміт блоків",
                "Поточний: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getInt("tnt." + tntType + ".max-blocks", -1),
                ChatColor.GRAY + "(-1 = без ліміту)",
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(32, limitButton);

        boolean particles = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".particles", true);
        ItemStack particlesButton = createToggleButton(Material.NETHER_STAR,
                ChatColor.YELLOW + "✨ Показувати частинки", particles);
        inv.setItem(37, particlesButton);

        ItemStack particleTypeButton = createButton(Material.FIREWORK_STAR,
                ChatColor.YELLOW + "🎆 Тип частинок",
                "Поточний: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".particle-type", "EXPLOSION_LARGE"),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(38, particleTypeButton);

        ItemStack particleCountButton = createButton(Material.GLOWSTONE_DUST,
                ChatColor.YELLOW + "💫 Кількість частинок",
                "Поточна: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getInt("tnt." + tntType + ".particle-count", 50),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(39, particleCountButton);

        boolean customSound = CustomTNT.getInstance().getTNTConfig().getBoolean("tnt." + tntType + ".custom-sound", false);
        ItemStack soundButton = createToggleButton(Material.NOTE_BLOCK,
                ChatColor.YELLOW + "🔊 Власний звук", customSound);
        inv.setItem(40, soundButton);

        ItemStack soundNameButton = createButton(Material.MUSIC_DISC_CAT,
                ChatColor.YELLOW + "🎵 Назва звуку",
                "Поточна: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getString("tnt." + tntType + ".sound-name", "ENTITY_GENERIC_EXPLODE"),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(41, soundNameButton);

        ItemStack volumeButton = createButton(Material.REPEATER,
                ChatColor.YELLOW + "📢 Гучність звуку",
                "Поточна: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".sound-volume", 1.0),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(42, volumeButton);

        ItemStack pitchButton = createButton(Material.COMPARATOR,
                ChatColor.YELLOW + "🎼 Висота звуку",
                "Поточна: " + ChatColor.WHITE + CustomTNT.getInstance().getTNTConfig().getDouble("tnt." + tntType + ".sound-pitch", 1.0),
                ChatColor.AQUA + "▶ Клікніть для зміни");
        inv.setItem(43, pitchButton);

        ItemStack craftButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftButton.getItemMeta();
        craftMeta.setDisplayName(ChatColor.GOLD + "⚒ Налаштувати крафт");
        List<String> craftLore = new ArrayList<>();
        craftLore.add(ChatColor.GRAY + "═══════════════════");
        boolean hasCraft = CustomTNT.getInstance().getTNTConfig().contains("tnt." + tntType + ".craft");
        if (hasCraft) {
            craftLore.add(ChatColor.GREEN + "✔ Крафт налаштовано");
        } else {
            craftLore.add(ChatColor.RED + "✖ Крафт не налаштовано");
        }
        craftLore.add(ChatColor.GRAY + "═══════════════════");
        craftLore.add(ChatColor.AQUA + "▶ Клікніть для налаштування");
        craftMeta.setLore(craftLore);
        craftButton.setItemMeta(craftMeta);
        inv.setItem(4, craftButton);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "← Назад");
        backButton.setItemMeta(backMeta);
        inv.setItem(49, backButton);

        // Декор
        ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        int[] decorSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 15, 16, 17, 18, 26, 27, 33, 34, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : decorSlots) {
            inv.setItem(slot, glass);
        }

        player.openInventory(inv);
    }

    private static ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.GRAY + "═══════════════════");
        for (String line : lore) {
            loreList.add(line);
        }
        loreList.add(ChatColor.GRAY + "═══════════════════");
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createToggleButton(Material material, String name, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "═══════════════════");
        lore.add(ChatColor.YELLOW + "Статус: " + (enabled ? ChatColor.GREEN + "Увімкнено ✔" : ChatColor.RED + "Вимкнено ✖"));
        lore.add(ChatColor.GRAY + "═══════════════════");
        lore.add(ChatColor.AQUA + "▶ Клікніть для перемикання");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(MAIN_MENU_TITLE)) {
            e.setCancelled(true);
            handleMainMenuClick(player, e);
        } else if (title.startsWith(EDIT_MENU_TITLE)) {
            e.setCancelled(true);
            handleEditMenuClick(player, e, title);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();

        if (title.equals(MAIN_MENU_TITLE) || title.startsWith(EDIT_MENU_TITLE)) {
            e.setCancelled(true);
        }
    }

    private void handleMainMenuClick(Player player, InventoryClickEvent e) {
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.EMERALD_BLOCK) {
            if (!player.hasPermission("customtnt.create")) {
                player.sendMessage(ChatColor.RED + "✘ У вас немає прав для створення TNT!");
                return;
            }
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "╔════════════════════════════════╗");
            player.sendMessage(ChatColor.GREEN + "║ " + ChatColor.YELLOW + "Напишіть ID для нового TNT    " + ChatColor.GREEN + "║");
            player.sendMessage(ChatColor.GREEN + "║ " + ChatColor.GRAY + "(без пробілів)" + ChatColor.GREEN + "               ║");
            player.sendMessage(ChatColor.GREEN + "╚════════════════════════════════╝");
            TNTCreationHandler.startCreation(player);
            return;
        }

        if (clicked.getType() == Material.TNT) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            NamespacedKey key = new NamespacedKey(CustomTNT.getInstance(), "tnt_id");
            String tntId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

            if (tntId == null || tntId.isEmpty()) {
                player.sendMessage(ChatColor.RED + "✘ Помилка: не вдалося визначити ID TNT!");
                return;
            }

            if (e.getClick() == ClickType.LEFT) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(CustomTNT.getInstance(), () -> {
                    openEditMenu(player, tntId);
                }, 2L);
            } else if (e.getClick() == ClickType.RIGHT) {
                if (!player.hasPermission("customtnt.create")) {
                    player.sendMessage(ChatColor.RED + "✘ У вас немає прав для видалення TNT!");
                    return;
                }

                CustomTNT.getInstance().getTNTConfig().set("tnt." + tntId, null);
                CustomTNT.getInstance().saveTNTConfig();

                CraftListener craftListener = CustomTNT.getInstance().getCraftListener();
                if (craftListener != null) {
                    craftListener.reloadRecipes();
                }

                player.sendMessage(ChatColor.GREEN + "✔ TNT '" + tntId + "' видалено!");
                player.closeInventory();

                Bukkit.getScheduler().runTaskLater(CustomTNT.getInstance(), () -> {
                    openMainMenu(player);
                }, 2L);
            }
        }
    }

    private void handleEditMenuClick(Player player, InventoryClickEvent e, String title) {
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String tntType = title.replace(EDIT_MENU_TITLE, "");
        int slot = e.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(CustomTNT.getInstance(), () -> {
                openMainMenu(player);
            }, 2L);
            return;
        }

        if (slot == 4) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(CustomTNT.getInstance(), () -> {
                // Використовуємо екземпляр з плагіна
                TNTCraftEditorGUI craftEditor = CustomTNT.getInstance().getCraftEditorGUI();
                if (craftEditor != null) {
                    craftEditor.openCraftEditor(player, tntType);
                } else {
                    player.sendMessage(ChatColor.RED + "✘ Помилка: редактор крафту не ініціалізовано!");
                }
            }, 2L);
            return;
        }

        String field = getFieldBySlot(slot);
        if (field != null) {
            if (isToggleField(field)) {
                toggleField(player, tntType, field);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(CustomTNT.getInstance(), () -> {
                    openEditMenu(player, tntType);
                }, 2L);
            } else {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "╔════════════════════════════════╗");
                player.sendMessage(ChatColor.GREEN + "║ " + ChatColor.YELLOW + "Напишіть нове значення в чат  " + ChatColor.GREEN + "║");
                player.sendMessage(ChatColor.GREEN + "╚════════════════════════════════╝");
                TNTEditHandler.startEdit(player, tntType, field);
            }
        }
    }

    private String getFieldBySlot(int slot) {
        switch (slot) {
            case 10: return "name";
            case 11: return "radius";
            case 12: return "power";
            case 13: return "fuse";
            case 14: return "description";
            case 19: return "fire";
            case 20: return "damage-blocks";
            case 21: return "auto-ignite";
            case 22: return "water-proof";
            case 23: return "break-obsidian";
            case 24: return "break-bedrock";
            case 25: return "ignore-protection";
            case 28: return "damage-entities";
            case 29: return "entity-damage-multiplier";
            case 30: return "drop-items";
            case 31: return "drop-chance";
            case 32: return "max-blocks";
            case 37: return "particles";
            case 38: return "particle-type";
            case 39: return "particle-count";
            case 40: return "custom-sound";
            case 41: return "sound-name";
            case 42: return "sound-volume";
            case 43: return "sound-pitch";
            default: return null;
        }
    }

    private boolean isToggleField(String field) {
        return field.equals("fire") || field.equals("damage-blocks") ||
                field.equals("auto-ignite") || field.equals("water-proof") ||
                field.equals("break-obsidian") || field.equals("break-bedrock") ||
                field.equals("ignore-protection") || field.equals("damage-entities") ||
                field.equals("drop-items") || field.equals("particles") ||
                field.equals("custom-sound");
    }

    private void toggleField(Player player, String tntType, String field) {
        String path = "tnt." + tntType + "." + field;
        boolean current = CustomTNT.getInstance().getTNTConfig().getBoolean(path, false);
        CustomTNT.getInstance().getTNTConfig().set(path, !current);
        CustomTNT.getInstance().saveTNTConfig();

        player.sendMessage(ChatColor.GREEN + "✔ Параметр оновлено!");
    }
}