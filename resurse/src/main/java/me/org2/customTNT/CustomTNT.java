package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomTNT extends JavaPlugin {

    private static CustomTNT instance;
    private File tntConfigFile;
    private FileConfiguration tntConfig;
    private CraftListener craftListener;
    private TNTCraftingGUI craftingGUI;
    private TNTCraftEditorGUI craftEditorGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        createTNTConfig();

        getCommand("tntcam").setExecutor(new TNTCommand(this));
        getCommand("tntcam").setTabCompleter(new TNTTabCompleter(this));

        getCommand("crafttnt").setExecutor(new CraftTNTCommand(this));

        // Реєстрація слухачів подій
        getServer().getPluginManager().registerEvents(new TNTListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTMenuGUI(), this);
        getServer().getPluginManager().registerEvents(new TNTEditHandler(), this);
        getServer().getPluginManager().registerEvents(new TNTCreationHandler(), this);

        craftListener = new CraftListener(this);
        getServer().getPluginManager().registerEvents(craftListener, this);

        craftingGUI = new TNTCraftingGUI(this);
        getServer().getPluginManager().registerEvents(craftingGUI, this);

        craftEditorGUI = new TNTCraftEditorGUI(this);
        getServer().getPluginManager().registerEvents(craftEditorGUI, this);

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║   CustomTNT Plugin v1.0 увімкнено!    ║");
        getLogger().info("║   Автор: nonentity1732                ║");
        getLogger().info("║   Використовуйте /crafttnt для крафту ║");
        getLogger().info("╚════════════════════════════════════════╝");

        checkIntegrations();
    }

    @Override
    public void onDisable() {
        saveTNTConfig();
        getLogger().info("CustomTNT Plugin вимкнено!");
    }

    private void createTNTConfig() {
        tntConfigFile = new File(getDataFolder(), "tnt.yml");
        if (!tntConfigFile.exists()) {
            tntConfigFile.getParentFile().mkdirs();
            saveResource("tnt.yml", false);
        }

        tntConfig = YamlConfiguration.loadConfiguration(tntConfigFile);
    }

    public void saveTNTConfig() {
        try {
            tntConfig.save(tntConfigFile);
        } catch (IOException e) {
            getLogger().severe("Не вдалося зберегти tnt.yml!");
            e.printStackTrace();
        }
    }

    public void reloadTNTConfig() {
        tntConfig = YamlConfiguration.loadConfiguration(tntConfigFile);
        reloadConfig();

        if (craftListener != null) {
            craftListener.reloadRecipes();
        }

        getLogger().info("Конфігурацію перезавантажено!");
    }

    public FileConfiguration getTNTConfig() {
        return tntConfig;
    }

    public CraftListener getCraftListener() {
        return craftListener;
    }

    public TNTCraftingGUI getCraftingGUI() {
        return craftingGUI;
    }

    public TNTCraftEditorGUI getCraftEditorGUI() {
        return craftEditorGUI;
    }

    public static CustomTNT getInstance() {
        return instance;
    }

    private void checkIntegrations() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            getLogger().info("✔ Виявлено Vault - економіка увімкнена");
        } else {
            getLogger().info("✘ Vault не знайдено - економіка вимкнена");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("✔ Виявлено PlaceholderAPI");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("✔ Виявлено WorldGuard - захист регіонів увімкнено");
        }

        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
            getLogger().info("✔ Виявлено GriefPrevention - захист земель увімкнено");
        }

        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            getLogger().info("✔ Виявлено Towny - захист міст увімкнено");
        }
    }
}