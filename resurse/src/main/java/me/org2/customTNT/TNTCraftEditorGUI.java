package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TNTCraftEditorGUI implements Listener {

    private final CustomTNT plugin;
    private static final String EDITOR_TITLE = ChatColor.GOLD + "⚒ Редактор крафту: ";

    private static final int[] CRAFT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private static final int PREVIEW_SLOT = 24;

    private final Map<UUID, String> editingTNT = new HashMap<>();

    public TNTCraftEditorGUI(CustomTNT plugin) {
        this.plugin = plugin;
    }

    public void openCraftEditor(Player player, String tntType) {
        Inventory inv = Bukkit.createInventory(null, 54, EDITOR_TITLE + tntType);

        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "ℹ Інформація");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "════════════════════");
        infoLore.add(ChatColor.YELLOW + "Покладіть предмети у");
        infoLore.add(ChatColor.YELLOW + "сітку 3x3 для створення");
        infoLore.add(ChatColor.YELLOW + "рецепту крафту");
        infoLore.add(ChatColor.GRAY + "════════════════════");
        infoLore.add(ChatColor.AQUA + "Для кастомного TNT:");
        infoLore.add(ChatColor.AQUA + "використайте звичайний TNT");
        infoLore.add(ChatColor.AQUA + "і Shift+ПКМ по ньому");
        infoMeta.setLore(infoLore);
        infoButton.setItemMeta(infoMeta);
        inv.setItem(4, infoButton);

        loadCurrentRecipe(inv, tntType);

        ItemStack preview = TNTManager.createTNTItem(tntType, 1);
        inv.setItem(PREVIEW_SLOT, preview);

        ItemStack saveButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "✔ Зберегти рецепт");
        List<String> saveLore = new ArrayList<>();
        saveLore.add(ChatColor.GRAY + "════════════════════");
        saveLore.add(ChatColor.YELLOW + "Натисніть, щоб зберегти");
        saveLore.add(ChatColor.YELLOW + "поточний рецепт");
        saveLore.add(ChatColor.GRAY + "════════════════════");
        saveMeta.setLore(saveLore);
        saveButton.setItemMeta(saveMeta);
        inv.setItem(48, saveButton);

        ItemStack deleteButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "✖ Видалити рецепт");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.GRAY + "════════════════════");
        deleteLore.add(ChatColor.YELLOW + "Видаляє рецепт крафту");
        deleteLore.add(ChatColor.YELLOW + "для цього TNT");
        deleteLore.add(ChatColor.GRAY + "════════════════════");
        deleteMeta.setLore(deleteLore);
        deleteButton.setItemMeta(deleteMeta);
        inv.setItem(50, deleteButton);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "← Назад");
        backButton.setItemMeta(backMeta);
        inv.setItem(49, backButton);

        ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (!isCraftSlot(i) && i != PREVIEW_SLOT && i != 4 && i != 48 && i != 49 && i != 50) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    private void loadCurrentRecipe(Inventory inv, String tntType) {
        ConfigurationSection craftSection = plugin.getTNTConfig()
                .getConfigurationSection("tnt." + tntType + ".craft");

        if (craftSection == null) return;

        List<String> shape = craftSection.getStringList("shape");
        ConfigurationSection ingredients = craftSection.getConfigurationSection("ingredients");

        if (shape.isEmpty() || ingredients == null) return;

        for (int row = 0; row < 3 && row < shape.size(); row++) {
            String shapeLine = shape.get(row);
            for (int col = 0; col < 3 && col < shapeLine.length(); col++) {
                char c = shapeLine.charAt(col);
                if (c == ' ') continue;

                String materialName = ingredients.getString(String.valueOf(c));
                if (materialName == null) continue;

                ItemStack item = null;

                if (materialName.startsWith("CUSTOM_TNT:")) {
                    String customTNTType = materialName.substring("CUSTOM_TNT:".length());
                    item = TNTManager.createTNTItem(customTNTType, 1);
                } else {
                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material != null) {
                        item = new ItemStack(material, 1);
                    }
                }

                if (item != null) {
                    int slotIndex = row * 3 + col;
                    inv.setItem(CRAFT_SLOTS[slotIndex], item);
                }
            }
        }
    }

    private static boolean isCraftSlot(int slot) {
        for (int craftSlot : CRAFT_SLOTS) {
            if (slot == craftSlot) return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (!title.startsWith(EDITOR_TITLE)) return;

        String tntType = title.replace(EDITOR_TITLE, "");
        int slot = e.getRawSlot();

        if (slot == 48) {
            e.setCancelled(true);
            saveRecipe(e.getInventory(), player, tntType);
            return;
        }

        if (slot == 50) {
            e.setCancelled(true);
            deleteRecipe(player, tntType);
            return;
        }

        if (slot == 49) {
            e.setCancelled(true);
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                TNTMenuGUI.openEditMenu(player, tntType);
            }, 2L);
            return;
        }

        if (slot < 54 && !isCraftSlot(slot)) {
            e.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (!title.startsWith(EDITOR_TITLE)) return;

        for (int slot : e.getRawSlots()) {
            if (slot < 54 && !isCraftSlot(slot)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (!title.startsWith(EDITOR_TITLE)) return;

        for (int slot : CRAFT_SLOTS) {
            ItemStack item = e.getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    for (ItemStack leftoverItem : leftover.values()) {
                        player.getWorld().dropItem(player.getLocation(), leftoverItem);
                    }
                }
            }
        }

        editingTNT.remove(player.getUniqueId());
    }

    private void saveRecipe(Inventory inv, Player player, String tntType) {
        ItemStack[] grid = new ItemStack[9];
        boolean isEmpty = true;

        for (int i = 0; i < CRAFT_SLOTS.length; i++) {
            grid[i] = inv.getItem(CRAFT_SLOTS[i]);
            if (grid[i] != null && grid[i].getType() != Material.AIR) {
                isEmpty = false;
            }
        }

        if (isEmpty) {
            player.sendMessage(ChatColor.RED + "✘ Сітка крафту порожня!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Створюємо рецепт
        List<String> shape = new ArrayList<>();
        Map<Character, String> ingredients = new HashMap<>();
        char currentChar = 'A';

        for (int row = 0; row < 3; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                ItemStack item = grid[row * 3 + col];
                if (item == null || item.getType() == Material.AIR) {
                    line.append(' ');
                } else {
                    String itemIdentifier;

                    if (TNTManager.isCustomTNT(item)) {
                        String customTNTType = TNTManager.getTNTType(item);
                        itemIdentifier = "CUSTOM_TNT:" + customTNTType;
                    } else {
                        itemIdentifier = item.getType().name();
                    }

                    char foundChar = ' ';
                    for (Map.Entry<Character, String> entry : ingredients.entrySet()) {
                        if (entry.getValue().equals(itemIdentifier)) {
                            foundChar = entry.getKey();
                            break;
                        }
                    }

                    if (foundChar == ' ') {
                        foundChar = currentChar;
                        ingredients.put(currentChar, itemIdentifier);
                        currentChar++;
                    }

                    line.append(foundChar);
                }
            }
            shape.add(line.toString());
        }

        String basePath = "tnt." + tntType + ".craft";
        plugin.getTNTConfig().set(basePath + ".amount", 1);
        plugin.getTNTConfig().set(basePath + ".shape", shape);

        for (Map.Entry<Character, String> entry : ingredients.entrySet()) {
            plugin.getTNTConfig().set(
                    basePath + ".ingredients." + entry.getKey(),
                    entry.getValue()
            );
        }

        plugin.saveTNTConfig();

        CraftListener craftListener = plugin.getCraftListener();
        if (craftListener != null) {
            craftListener.reloadRecipes();
        }

        player.sendMessage(ChatColor.GREEN + "✔ Рецепт успішно збережено!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TNTMenuGUI.openEditMenu(player, tntType);
        }, 2L);
    }

    private void deleteRecipe(Player player, String tntType) {
        String basePath = "tnt." + tntType + ".craft";

        if (!plugin.getTNTConfig().contains(basePath)) {
            player.sendMessage(ChatColor.RED + "✘ Рецепт не знайдено!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        plugin.getTNTConfig().set(basePath, null);
        plugin.saveTNTConfig();

        CraftListener craftListener = plugin.getCraftListener();
        if (craftListener != null) {
            craftListener.reloadRecipes();
        }

        player.sendMessage(ChatColor.GREEN + "✔ Рецепт видалено!");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);

        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TNTMenuGUI.openEditMenu(player, tntType);
        }, 2L);
    }
}