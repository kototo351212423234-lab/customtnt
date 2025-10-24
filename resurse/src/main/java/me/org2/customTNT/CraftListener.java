package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.CraftingInventory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CraftListener implements Listener {

    private final CustomTNT plugin;
    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    public CraftListener(CustomTNT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getView().getPlayer();
        CraftingInventory inv = e.getInventory();

        if (e.getRecipe() == null || e.getRecipe().getResult() == null) {
            return;
        }

        ItemStack result = e.getRecipe().getResult();

        if (TNTManager.isCustomTNT(result)) {
            inv.setResult(null);
            player.sendMessage(ChatColor.RED + "✘ Кастомний TNT можна крафтити лише через /crafttnt!");
            return;
        }

        ItemStack[] matrix = inv.getMatrix();
        for (ItemStack item : matrix) {
            if (item != null && TNTManager.isCustomTNT(item)) {
                inv.setResult(null);
                player.sendMessage(ChatColor.RED + "✘ Використовуйте /crafttnt для крафту кастомного TNT!");
                return;
            }
        }
    }

    public void reloadRecipes() {
        plugin.getLogger().info("Рецепти для звичайного верстака заблоковані");
        plugin.getLogger().info("Використовуйте /crafttnt для крафту кастомних TNT");
    }
}