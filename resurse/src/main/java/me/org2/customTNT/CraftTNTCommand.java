package me.org2.customTNT;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftTNTCommand implements CommandExecutor {

    private final CustomTNT plugin;

    public CraftTNTCommand(CustomTNT plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Цю команду може використовувати тільки гравець!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("customtnt.craft")) {
            player.sendMessage(ChatColor.RED + "У вас немає прав для крафту TNT!");
            return true;
        }

        plugin.getCraftingGUI().openCraftingMenu(player);
        return true;
    }
}