package me.org2.customTNT;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class TNTTabCompleter implements TabCompleter {

    private final CustomTNT plugin;

    public TNTTabCompleter(CustomTNT plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            completions.add("menu");
            completions.add("craft");
            completions.add("editor");
            completions.add("reload");
            completions.add("give");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (args[0].equalsIgnoreCase("editor")) {
                completions.add("main");
                completions.add("edit");
                completions.add("craft");
                completions.add("reset");
                completions.add("reload");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (plugin.getTNTConfig().contains("tnt")) {
                completions.addAll(plugin.getTNTConfig().getConfigurationSection("tnt").getKeys(false));
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("8");
            completions.add("16");
            completions.add("32");
            completions.add("64");
        }

        List<String> result = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();
        for (String s : completions) {
            if (s.toLowerCase().startsWith(input)) {
                result.add(s);
            }
        }

        return result;
    }
}