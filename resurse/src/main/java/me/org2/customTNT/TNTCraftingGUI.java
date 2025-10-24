package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.*;

public class TNTCraftingGUI implements Listener {

    private static final String WORKBENCH_TITLE = ChatColor.GOLD + "⚒ Крафт TNT";
    private final Map<UUID, CraftingSession> sessions = new HashMap<>();
    private Object economy = null;
    private boolean vaultEnabled = false;
    private File dataFile;
    private FileConfiguration playerData;
    private final CustomTNT plugin;

    public TNTCraftingGUI(CustomTNT plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
                RegisteredServiceProvider<?> rsp = plugin.getServer()
                        .getServicesManager().getRegistration(economyClass);
                if (rsp != null) {
                    economy = rsp.getProvider();
                    vaultEnabled = true;
                    plugin.getLogger().info("Vault Economy успішно підключено!");
                }
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("Vault не знайдено! Економіка вимкнена.");
                vaultEnabled = false;
            }
        }

        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void openCraftingMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, WORKBENCH_TITLE);

        int[] craftSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        ItemStack craftButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftButtonMeta = craftButton.getItemMeta();
        craftButtonMeta.setDisplayName(ChatColor.GREEN + "✔ Скрафтити");
        List<String> craftLore = new ArrayList<>();
        craftLore.add(ChatColor.GRAY + "═══════════════════");
        craftLore.add(ChatColor.YELLOW + "Поставте предмети в крафт");
        craftLore.add(ChatColor.YELLOW + "та натисніть для крафту");
        craftLore.add(ChatColor.GRAY + "═══════════════════");
        craftButtonMeta.setLore(craftLore);
        craftButton.setItemMeta(craftButtonMeta);
        inv.setItem(24, craftButton);

        updateResultSlot(inv, new ItemStack[9]);

        updateChanceDisplay(inv, player);

        updateUpgradeButton(inv, player);

        ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        int[] decorSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 13, 17, 18, 22, 26, 27, 31, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : decorSlots) {
            inv.setItem(slot, glass);
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "✖ Закрити");
        closeButton.setItemMeta(closeMeta);
        inv.setItem(49, closeButton);

        player.openInventory(inv);
        sessions.put(player.getUniqueId(), new CraftingSession());
    }

    private void updateResultSlot(Inventory inv, ItemStack[] matrix) {
        String tntType = findMatchingRecipe(matrix);

        if (tntType != null) {
            int amount = plugin.getTNTConfig().getInt("tnt." + tntType + ".craft.amount", 1);
            ItemStack result = TNTManager.createTNTItem(tntType, amount);
            inv.setItem(16, result);
        } else {
            ItemStack resultSlot = new ItemStack(Material.BARRIER);
            ItemMeta resultMeta = resultSlot.getItemMeta();
            resultMeta.setDisplayName(ChatColor.YELLOW + "❓ Результат");
            List<String> resultLore = new ArrayList<>();
            resultLore.add(ChatColor.GRAY + "Тут з'явиться результат");
            resultMeta.setLore(resultLore);
            resultSlot.setItemMeta(resultMeta);
            inv.setItem(16, resultSlot);
        }
    }

    private void updateChanceDisplay(Inventory inv, Player player) {
        int level = getCraftLevel(player);
        double chance = getSuccessChance(level);

        ItemStack chanceItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta chanceMeta = chanceItem.getItemMeta();
        chanceMeta.setDisplayName(ChatColor.GOLD + "⭐ Шанс успіху");
        List<String> chanceLore = new ArrayList<>();
        chanceLore.add(ChatColor.GRAY + "═══════════════════");
        chanceLore.add(ChatColor.YELLOW + "Поточний рівень: " + ChatColor.WHITE + level);
        chanceLore.add(ChatColor.YELLOW + "Шанс крафту: " + ChatColor.GREEN + (int)(chance * 100) + "%");
        chanceLore.add(ChatColor.GRAY + "═══════════════════");
        chanceLore.add(ChatColor.AQUA + "Покращуйте рівень для");
        chanceLore.add(ChatColor.AQUA + "збільшення шансу!");
        chanceMeta.setLore(chanceLore);
        chanceItem.setItemMeta(chanceMeta);
        inv.setItem(4, chanceItem);
    }

    private void updateUpgradeButton(Inventory inv, Player player) {
        int level = getCraftLevel(player);
        int nextLevel = level + 1;
        double currentChance = getSuccessChance(level);
        double nextChance = getSuccessChance(nextLevel);

        int xpCost = getUpgradeCost(nextLevel, true);
        double moneyCost = getUpgradeCost(nextLevel, false);

        ItemStack upgradeButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta upgradeMeta = upgradeButton.getItemMeta();
        upgradeMeta.setDisplayName(ChatColor.GREEN + "⬆ Покращити рівень");
        List<String> upgradeLore = new ArrayList<>();
        upgradeLore.add(ChatColor.GRAY + "═══════════════════");
        upgradeLore.add(ChatColor.YELLOW + "Поточний рівень: " + ChatColor.WHITE + level);
        upgradeLore.add(ChatColor.YELLOW + "Шанс: " + ChatColor.GREEN + (int)(currentChance * 100) + "%");
        upgradeLore.add("");

        if (nextLevel <= 10) {
            upgradeLore.add(ChatColor.GOLD + "Наступний рівень: " + ChatColor.WHITE + nextLevel);
            upgradeLore.add(ChatColor.GOLD + "Новий шанс: " + ChatColor.GREEN + (int)(nextChance * 100) + "%");
            upgradeLore.add("");
            upgradeLore.add(ChatColor.YELLOW + "Вартість:");
            upgradeLore.add(ChatColor.GREEN + " ◆ Рівень: " + ChatColor.WHITE + xpCost);
            if (vaultEnabled) {
                upgradeLore.add(ChatColor.GREEN + " ◆ Гроші: " + ChatColor.WHITE + moneyCost + "$");
            }
            upgradeLore.add("");
            upgradeLore.add(ChatColor.AQUA + "▶ Клікніть для покращення");
        } else {
            upgradeLore.add(ChatColor.RED + "Максимальний рівень!");
        }

        upgradeLore.add(ChatColor.GRAY + "═══════════════════");
        upgradeMeta.setLore(upgradeLore);
        upgradeButton.setItemMeta(upgradeMeta);
        inv.setItem(34, upgradeButton);
    }

    private int getCraftLevel(Player player) {
        return playerData.getInt("players." + player.getUniqueId() + ".craft-level", 1);
    }

    private void setCraftLevel(Player player, int level) {
        playerData.set("players." + player.getUniqueId() + ".craft-level", level);
        savePlayerData();
    }

    private void savePlayerData() {
        try {
            playerData.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getSuccessChance(int level) {
        return Math.min(0.10 + (level - 1) * 0.09, 0.91);
    }

    private int getUpgradeCost(int level, boolean isXP) {
        if (isXP) {
            return level * 5;
        } else {
            return (int)(1000 * Math.pow(level, 1.5));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (!title.equals(WORKBENCH_TITLE)) return;

        int slot = e.getRawSlot();
        int[] craftSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        boolean isCraftSlot = false;
        for (int craftSlot : craftSlots) {
            if (slot == craftSlot) {
                isCraftSlot = true;
                break;
            }
        }

        if (isCraftSlot || slot >= 54) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack[] matrix = new ItemStack[9];
                for (int i = 0; i < craftSlots.length; i++) {
                    matrix[i] = e.getInventory().getItem(craftSlots[i]);
                }
                updateResultSlot(e.getInventory(), matrix);
            }, 1L);
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        // Кнопка покращення
        if (slot == 34) {
            handleUpgrade(player, e.getInventory());
            return;
        }

        if (slot == 24) {
            handleCraft(player, e.getInventory());
            return;
        }
    }

    private void handleUpgrade(Player player, Inventory inv) {
        int currentLevel = getCraftLevel(player);

        if (currentLevel >= 10) {
            player.sendMessage(ChatColor.RED + "✘ Ви досягли максимального рівня!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int nextLevel = currentLevel + 1;
        int xpCost = getUpgradeCost(nextLevel, true);
        double moneyCost = getUpgradeCost(nextLevel, false);

        if (player.getLevel() < xpCost) {
            player.sendMessage(ChatColor.RED + "✘ Недостатньо рівнів! Потрібно: " + xpCost);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (vaultEnabled && economy != null) {
            try {
                java.lang.reflect.Method hasMethod = economy.getClass().getMethod("has",
                        org.bukkit.OfflinePlayer.class, double.class);
                Boolean hasEnough = (Boolean) hasMethod.invoke(economy, player, moneyCost);

                if (!hasEnough) {
                    player.sendMessage(ChatColor.RED + "✘ Недостатньо грошей! Потрібно: " + moneyCost + "$");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                java.lang.reflect.Method withdrawMethod = economy.getClass().getMethod("withdrawPlayer",
                        org.bukkit.OfflinePlayer.class, double.class);
                withdrawMethod.invoke(economy, player, moneyCost);

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✘ Помилка при роботі з економікою!");
                e.printStackTrace();
                return;
            }
        }

        player.setLevel(player.getLevel() - xpCost);
        setCraftLevel(player, nextLevel);

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "╔════════════════════════════════╗");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.GOLD + "✔ Рівень покращено!           " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "Новий рівень: " + ChatColor.WHITE + nextLevel + "           " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "║  " + ChatColor.YELLOW + "Шанс крафту: " + ChatColor.GREEN + (int)(getSuccessChance(nextLevel) * 100) + "%    " + ChatColor.GREEN + "║");
        player.sendMessage(ChatColor.GREEN + "╚════════════════════════════════╝");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        updateChanceDisplay(inv, player);
        updateUpgradeButton(inv, player);
    }

    private void handleCraft(Player player, Inventory inv) {
        int[] craftSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        ItemStack[] matrix = new ItemStack[9];

        for (int i = 0; i < craftSlots.length; i++) {
            ItemStack item = inv.getItem(craftSlots[i]);
            if (item != null && item.getType() != Material.AIR) {
                matrix[i] = item.clone();
            }
        }

        String tntType = findMatchingRecipe(matrix);

        if (tntType == null) {
            player.sendMessage(ChatColor.RED + "✘ Невірний рецепт крафту!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!player.hasPermission("customtnt.craft." + tntType)) {
            player.sendMessage(ChatColor.RED + "✘ У вас немає прав для крафту цього TNT!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int level = getCraftLevel(player);
        double chance = getSuccessChance(level);
        boolean success = Math.random() < chance;

        if (success) {
            for (int slot : craftSlots) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        inv.setItem(slot, null);
                    }
                }
            }

            int amount = plugin.getTNTConfig().getInt("tnt." + tntType + ".craft.amount", 1);
            ItemStack result = TNTManager.createTNTItem(tntType, amount);
            player.getInventory().addItem(result);

            player.sendMessage(ChatColor.GREEN + "✔ Крафт успішний! Ви отримали " + amount + "x " + tntType);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            for (int slot : craftSlots) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        inv.setItem(slot, null);
                    }
                }
            }

            player.sendMessage(ChatColor.RED + "✘ Крафт не вдався! Спробуйте ще раз.");
            player.sendMessage(ChatColor.YELLOW + "Підказка: Покращте рівень для більшого шансу успіху!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        }

        ItemStack[] newMatrix = new ItemStack[9];
        for (int i = 0; i < craftSlots.length; i++) {
            newMatrix[i] = inv.getItem(craftSlots[i]);
        }
        updateResultSlot(inv, newMatrix);
    }

    private String findMatchingRecipe(ItemStack[] matrix) {
        org.bukkit.configuration.ConfigurationSection tntSection = plugin.getTNTConfig()
                .getConfigurationSection("tnt");

        if (tntSection == null) return null;

        for (String tntType : tntSection.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection craftSection = tntSection.getConfigurationSection(tntType + ".craft");
            if (craftSection == null) continue;

            List<String> shape = craftSection.getStringList("shape");
            if (shape.isEmpty() || shape.size() != 3) continue;

            org.bukkit.configuration.ConfigurationSection ingredients = craftSection.getConfigurationSection("ingredients");
            if (ingredients == null) continue;

            // Перевіряємо чи збігається рецепт
            if (matchesRecipe(matrix, shape, ingredients)) {
                return tntType;
            }
        }

        return null;
    }

    private boolean matchesRecipe(ItemStack[] matrix, List<String> shape, org.bukkit.configuration.ConfigurationSection ingredients) {
        for (int row = 0; row < 3; row++) {
            String shapeLine = shape.get(row);
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                ItemStack item = matrix[index];

                char shapeChar = col < shapeLine.length() ? shapeLine.charAt(col) : ' ';

                if (shapeChar == ' ') {
                    if (item != null && item.getType() != Material.AIR) {
                        return false;
                    }
                } else {
                    String materialName = ingredients.getString(String.valueOf(shapeChar));
                    if (materialName == null) return false;

                    if (materialName.startsWith("CUSTOM_TNT:")) {
                        String requiredTNTType = materialName.substring("CUSTOM_TNT:".length());

                        if (item == null || item.getType() != Material.TNT) {
                            return false;
                        }

                        String itemTNTType = TNTManager.getTNTType(item);
                        if (itemTNTType == null || !itemTNTType.equals(requiredTNTType)) {
                            return false;
                        }
                    } else {
                        Material requiredMaterial = Material.getMaterial(materialName.toUpperCase());
                        if (requiredMaterial == null) return false;

                        if (item == null || item.getType() != requiredMaterial) {
                            return false;
                        }

                        if (requiredMaterial == Material.TNT && TNTManager.isCustomTNT(item)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equals(WORKBENCH_TITLE)) return;

        int[] craftSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int slot : craftSlots) {
            ItemStack item = e.getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }

        sessions.remove(player.getUniqueId());
    }

    private static class CraftingSession {
    }
}