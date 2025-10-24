package me.org2.customTNT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TNTListener implements Listener {

    private final CustomTNT plugin;
    private final Random random = new Random();
    private Object griefPrevention = null;
    private boolean hasGriefPrevention = false;

    public TNTListener(CustomTNT plugin) {
        this.plugin = plugin;

        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
            try {
                Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
                griefPrevention = Bukkit.getPluginManager().getPlugin("GriefPrevention");
                hasGriefPrevention = true;
                plugin.getLogger().info("GriefPrevention інтеграція активована!");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("GriefPrevention знайдено, але не вдалося підключити API");
            }
        }
    }

    @EventHandler
    public void onTNTPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItemInHand();

        if (!TNTManager.isCustomTNT(item)) {
            return;
        }

        String tntType = TNTManager.getTNTType(item);
        if (tntType == null) {
            return;
        }

        ConfigurationSection config = plugin.getTNTConfig().getConfigurationSection("tnt." + tntType);
        if (config == null) {
            player.sendMessage(ChatColor.RED + "✘ Помилка: TNT типу '" + tntType + "' не знайдено!");
            e.setCancelled(true);
            return;
        }

        if (!player.hasPermission("customtnt.place") && !player.hasPermission("customtnt.place." + tntType)) {
            player.sendMessage(ChatColor.RED + "✘ У вас немає прав для встановлення цього TNT!");
            e.setCancelled(true);
            return;
        }

        boolean autoIgnite = config.getBoolean("auto-ignite", true);

        if (autoIgnite) {
            e.setCancelled(true);

            Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
            TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
            int fuse = config.getInt("fuse", 80);
            tnt.setFuseTicks(fuse);
            tnt.setMetadata("customTNT", new FixedMetadataValue(plugin, tntType));
            tnt.setMetadata("tntPlacer", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

            player.sendMessage(ChatColor.GREEN + "✔ Встановлено кастомний TNT: " +
                    ChatColor.translateAlternateColorCodes('&', config.getString("name", tntType)));
        } else {
            Block block = e.getBlockPlaced();

            block.setMetadata("customTNT", new FixedMetadataValue(plugin, tntType));
            block.setMetadata("tntPlacer", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

            player.sendMessage(ChatColor.GREEN + "✔ Встановлено кастомний TNT: " +
                    ChatColor.translateAlternateColorCodes('&', config.getString("name", tntType)) +
                    ChatColor.GRAY + " (підпаліть для активації)");
        }
    }

    @EventHandler
    public void onTNTIgnite(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.TNT) {
            return;
        }

        if (!block.hasMetadata("customTNT")) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.FLINT_AND_STEEL &&
                item.getType() != Material.FIRE_CHARGE) {
            return;
        }

        String tntType = block.getMetadata("customTNT").get(0).asString();
        String placerUUID = null;

        if (block.hasMetadata("tntPlacer")) {
            placerUUID = block.getMetadata("tntPlacer").get(0).asString();
        }

        ConfigurationSection config = plugin.getTNTConfig().getConfigurationSection("tnt." + tntType);
        if (config == null) {
            return;
        }

        e.setCancelled(true);

        block.setType(Material.AIR);

        Location loc = block.getLocation().add(0.5, 0, 0.5);
        TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
        int fuse = config.getInt("fuse", 80);
        tnt.setFuseTicks(fuse);
        tnt.setMetadata("customTNT", new FixedMetadataValue(plugin, tntType));

        if (placerUUID != null) {
            tnt.setMetadata("tntPlacer", new FixedMetadataValue(plugin, placerUUID));
        } else {
            tnt.setMetadata("tntPlacer", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        }

        if (item.getType() == Material.FLINT_AND_STEEL &&
                player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                item.setAmount(0);
            } else {
                item.setDurability((short)(item.getDurability() + 1));
            }
        } else if (item.getType() == Material.FIRE_CHARGE &&
                player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        loc.getWorld().playSound(loc, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);

        player.sendMessage(ChatColor.YELLOW + "✔ TNT підпалено!");
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent e) {
        if (e.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }

        TNTPrimed tnt = (TNTPrimed) e.getEntity();

        if (!tnt.hasMetadata("customTNT")) {
            return;
        }

        String tntType = tnt.getMetadata("customTNT").get(0).asString();
        ConfigurationSection config = plugin.getTNTConfig().getConfigurationSection("tnt." + tntType);

        if (config == null) {
            return;
        }

        String placerUUID = null;
        if (tnt.hasMetadata("tntPlacer")) {
            placerUUID = tnt.getMetadata("tntPlacer").get(0).asString();
        }

        Location loc = tnt.getLocation();

        double radius = config.getDouble("radius", 4.0);
        double power = config.getDouble("power", 4.0);
        boolean fire = config.getBoolean("fire", false);
        boolean damageBlocks = config.getBoolean("damage-blocks", true);
        boolean breakObsidian = config.getBoolean("break-obsidian", false);
        boolean breakBedrock = config.getBoolean("break-bedrock", false);
        boolean waterProof = config.getBoolean("water-proof", false);
        boolean damageEntities = config.getBoolean("damage-entities", true);
        boolean ignoreProtection = config.getBoolean("ignore-protection", false);
        double damageMultiplier = config.getDouble("entity-damage-multiplier", 1.0);
        int maxBlocks = config.getInt("max-blocks", -1);
        boolean dropItems = config.getBoolean("drop-items", false);
        double dropChance = config.getDouble("drop-chance", 0.3);
        boolean particles = config.getBoolean("particles", true);
        String particleType = config.getString("particle-type", "EXPLOSION_LARGE");
        int particleCount = config.getInt("particle-count", 50);
        boolean customSound = config.getBoolean("custom-sound", false);
        String soundName = config.getString("sound-name", "ENTITY_GENERIC_EXPLODE");
        double soundVolume = config.getDouble("sound-volume", 1.0);
        double soundPitch = config.getDouble("sound-pitch", 1.0);

        e.setCancelled(true);

        if (!waterProof && isInWater(loc)) {
            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
            return;
        }

        loc.getWorld().createExplosion(loc, 0.0F, false, false);

        if (damageBlocks) {
            List<Block> blocksToBreak = calculateExplosionBlocks(loc, radius, power, breakObsidian, breakBedrock, maxBlocks, ignoreProtection, placerUUID);

            for (Block block : blocksToBreak) {
                try {
                    Material type = block.getType();

                    if (!ignoreProtection && !canBreakBlock(block, placerUUID)) {
                        continue;
                    }

                    if (dropItems && Math.random() < dropChance) {
                        block.breakNaturally();
                    } else {
                        block.setType(Material.AIR);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().severe("Помилка при знищенні блоку: " + ex.getMessage());
                }
            }
        }

        if (damageEntities) {
            damageNearbyEntities(loc, radius, damageMultiplier);
        }

        if (fire) {
            createFire(loc, radius, ignoreProtection, placerUUID);
        }

        if (particles) {
            spawnParticles(loc, particleType, particleCount);
        }

        if (customSound) {
            playCustomSound(loc, soundName, soundVolume, soundPitch);
        } else {
            playSound(loc, power);
        }
    }

    private boolean isInWater(Location loc) {
        Block block = loc.getBlock();
        Material type = block.getType();

        return type == Material.WATER ||
                type == Material.KELP ||
                type == Material.KELP_PLANT ||
                type == Material.SEAGRASS ||
                type == Material.TALL_SEAGRASS ||
                type == Material.BUBBLE_COLUMN;
    }

    private boolean canBreakBlock(Block block, String placerUUID) {
        if (hasGriefPrevention && griefPrevention != null) {
            try {
                Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
                Object gpInstance = griefPrevention;

                java.lang.reflect.Method getDataStoreMethod = gpClass.getMethod("dataStore");
                Object dataStore = getDataStoreMethod.invoke(gpInstance);

                java.lang.reflect.Method getClaimMethod = dataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, Object.class);
                Object claim = getClaimMethod.invoke(dataStore, block.getLocation(), false, null);

                if (claim != null) {
                    if (placerUUID != null) {
                        try {
                            UUID uuid = UUID.fromString(placerUUID);
                            Player placer = Bukkit.getPlayer(uuid);

                            if (placer != null) {
                                java.lang.reflect.Method allowBreakMethod = claim.getClass().getMethod("allowBreak", Player.class, Material.class);
                                String result = (String) allowBreakMethod.invoke(claim, placer, block.getType());
                                return result == null;
                            }
                        } catch (IllegalArgumentException ex) {
                            plugin.getLogger().warning("Невірний UUID гравця: " + placerUUID);
                        }
                    }
                    return false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Помилка при перевірці GriefPrevention: " + e.getMessage());
            }
        }

        return true;
    }

    private List<Block> calculateExplosionBlocks(Location center, double radius, double power,
                                                 boolean breakObsidian, boolean breakBedrock, int maxBlocks,
                                                 boolean ignoreProtection, String placerUUID) {
        List<Block> affectedBlocks = new ArrayList<>();

        int raysPerAxis = 16;
        double step = 1.0 / raysPerAxis;

        for (double x = 0; x <= 1.0; x += step) {
            for (double y = 0; y <= 1.0; y += step) {
                for (double z = 0; z <= 1.0; z += step) {
                    if (x != 0 && x != 1 && y != 0 && y != 1 && z != 0 && z != 1) {
                        continue;
                    }

                    Vector direction = new Vector(x * 2 - 1, y * 2 - 1, z * 2 - 1);
                    if (direction.lengthSquared() < 0.0001) continue;
                    direction.normalize();

                    double rayPower = power * (0.7 + random.nextDouble() * 0.6);
                    Vector currentPos = center.toVector().clone();
                    double stepSize = 0.3;

                    while (rayPower > 0) {
                        currentPos.add(direction.clone().multiply(stepSize));

                        if (currentPos.distance(center.toVector()) > radius) {
                            break;
                        }

                        Block block = center.getWorld().getBlockAt(
                                currentPos.getBlockX(),
                                currentPos.getBlockY(),
                                currentPos.getBlockZ()
                        );

                        Material type = block.getType();

                        if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) {
                            rayPower -= 0.3;
                            continue;
                        }

                        if (type == Material.BEDROCK || type == Material.BARRIER) {
                            if (!breakBedrock) {
                                break;
                            }
                            if (!affectedBlocks.contains(block)) {
                                affectedBlocks.add(block);
                                if (maxBlocks > 0 && affectedBlocks.size() >= maxBlocks) {
                                    return affectedBlocks;
                                }
                            }
                            rayPower -= 1.0;
                            continue;
                        }

                        // ===== ПЕРЕВІРКА ОБСИДІАНА =====
                        if (type == Material.OBSIDIAN || type == Material.CRYING_OBSIDIAN) {
                            if (!breakObsidian) {
                                break; // Зупиняємо промінь якщо НЕ можна ламати обсидіан
                            }
                            // Якщо breakObsidian = true, додаємо блок і продовжуємо
                            if (!affectedBlocks.contains(block)) {
                                affectedBlocks.add(block);
                                if (maxBlocks > 0 && affectedBlocks.size() >= maxBlocks) {
                                    return affectedBlocks;
                                }
                            }
                            rayPower -= 0.8;
                            continue;
                        }

                        double resistance = getBlockResistance(type);
                        rayPower -= (resistance / power) * 3.0;

                        if (rayPower > 0 && !affectedBlocks.contains(block)) {
                            affectedBlocks.add(block);

                            if (maxBlocks > 0 && affectedBlocks.size() >= maxBlocks) {
                                return affectedBlocks;
                            }
                        }

                        rayPower -= 0.75;
                    }
                }
            }
        }

        return affectedBlocks;
    }

    private double getBlockResistance(Material type) {
        switch (type) {
            case OBSIDIAN:
            case CRYING_OBSIDIAN:
                return 1200.0;
            case BEDROCK:
            case BARRIER:
                return 3600000.0;
            case STONE:
            case COBBLESTONE:
            case DEEPSLATE:
                return 6.0;
            case DIRT:
            case GRASS_BLOCK:
            case SAND:
            case GRAVEL:
                return 0.5;
            default:
                return 3.0;
        }
    }

    private void damageNearbyEntities(Location loc, double radius, double multiplier) {
        loc.getWorld().getNearbyEntities(loc, radius, radius, radius).forEach(entity -> {
            if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof TNTPrimed)) {
                org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                double distance = entity.getLocation().distance(loc);

                if (distance < radius) {
                    double impact = 1.0 - (distance / radius);
                    double damage = ((impact * impact + impact) / 2.0) * 7.0 * (radius * 2.0) + 1.0;
                    damage *= multiplier;

                    if (damage > 0) {
                        living.damage(damage);

                        Vector direction = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                        double knockback = impact * 0.5;
                        entity.setVelocity(direction.multiply(knockback));
                    }
                }
            }
        });
    }

    private void createFire(Location loc, double radius, boolean ignoreProtection, String placerUUID) {
        int fireRadius = (int) Math.ceil(radius / 2);
        for (int x = -fireRadius; x <= fireRadius; x++) {
            for (int y = -fireRadius; y <= fireRadius; y++) {
                for (int z = -fireRadius; z <= fireRadius; z++) {
                    Location fireLoc = loc.clone().add(x, y, z);
                    double distance = fireLoc.distance(loc);

                    if (distance <= radius / 2) {
                        Block block = fireLoc.getBlock();
                        if (block.getType() == Material.AIR && Math.random() < 0.3) {
                            if (!ignoreProtection && !canBreakBlock(block, placerUUID)) {
                                continue;
                            }
                            block.setType(Material.FIRE);
                        }
                    }
                }
            }
        }
    }

    private void spawnParticles(Location loc, String particleType, int count) {
        try {
            Particle particle = Particle.valueOf(particleType.toUpperCase());
            loc.getWorld().spawnParticle(particle, loc, count, 1.0, 1.0, 1.0, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Невідомий тип частинок: " + particleType);
            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, count, 1.0, 1.0, 1.0, 0.1);
        }
    }

    private void playCustomSound(Location loc, String soundName, double volume, double pitch) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            loc.getWorld().playSound(loc, sound, (float)volume, (float)pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Невідомий звук: " + soundName);
            playSound(loc, 4.0);
        }
    }

    private void playSound(Location loc, double power) {
        float pitch = (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F;
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 4.0F, pitch);
    }
}