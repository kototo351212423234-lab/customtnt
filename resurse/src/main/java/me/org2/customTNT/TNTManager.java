package me.org2.customTNT;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

public class TNTManager {

    public static ItemStack createTNTItem(String tntType, int amount) {
        ItemStack tnt = new ItemStack(Material.TNT, amount);
        ItemMeta meta = tnt.getItemMeta();

        ConfigurationSection config = CustomTNT.getInstance().getTNTConfig().getConfigurationSection("tnt." + tntType);

        if (config == null) {
            return tnt;
        }

        String name = config.getString("name", tntType);
        double radius = config.getDouble("radius", 4.0);
        double power = config.getDouble("power", 4.0);
        int fuse = config.getInt("fuse", 80);
        boolean fire = config.getBoolean("fire", false);
        boolean damageBlocks = config.getBoolean("damage-blocks", true);
        String description = config.getString("description", "");

        meta.setDisplayName(ChatColor.RED + "☢ " + ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "═══════════════════");
        lore.add(ChatColor.DARK_GRAY + "Тип: " + ChatColor.YELLOW + tntType);

        if (!description.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', description));
        }

        lore.add("");
        lore.add(ChatColor.GOLD + "Характеристики:");
        lore.add(ChatColor.YELLOW + " ◆ Радіус: " + ChatColor.WHITE + radius);
        lore.add(ChatColor.YELLOW + " ◆ Сила: " + ChatColor.WHITE + power);
        lore.add(ChatColor.YELLOW + " ◆ Вибух: " + ChatColor.WHITE + (fuse / 20.0) + "с");
        lore.add(ChatColor.YELLOW + " ◆ Вогонь: " + (fire ? ChatColor.GREEN + "Так" : ChatColor.RED + "Ні"));
        lore.add(ChatColor.YELLOW + " ◆ Руйнування: " + (damageBlocks ? ChatColor.GREEN + "Так" : ChatColor.RED + "Ні"));
        lore.add(ChatColor.GRAY + "═══════════════════");

        meta.setLore(lore);
        tnt.setItemMeta(meta);

        return tnt;
    }

    public static String getTNTType(ItemStack item) {
        if (item == null || item.getType() != Material.TNT) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return null;
        }

        List<String> lore = meta.getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Тип:")) {
                return stripped.replace("Тип:", "").trim();
            }
        }

        return null;
    }

    public static boolean isCustomTNT(ItemStack item) {
        return getTNTType(item) != null;
    }
}