package me.rin.nightshowl;

import lombok.Getter;
import me.nologic.minority.MinorityFeature;
import me.rin.nightshowl.entities.WerewolfEntity;
import me.rin.nightshowl.entities.WolfEntity;
import me.rin.nightshowl.items.JSONLootable;
import me.rin.nightshowl.utils.MessageManager;
import me.rin.nightshowl.utils.WolvesConfigManager;
import me.rin.nightshowl.utils.statics.Chance;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WolvesBehaviorHandler implements MinorityFeature, Listener {

    private final NightsHowl              instance;
    private final MessageManager          messageManager;
    private final WolvesConfigManager     configManager;

    private final ArrayList<Player>       playersKilledByWolves;
    private final HashMap<World, Boolean> worldTimeSwitch;
    private final ArrayList<Entity>       createdWolves;

    // Initialization
    public WolvesBehaviorHandler(final NightsHowl plugin) {

        this.instance =              plugin;
        this.messageManager =        plugin.getMessageManager();
        this.configManager =         plugin.getConfigManager();

        this.playersKilledByWolves = new ArrayList<>();
        this.worldTimeSwitch =       new HashMap<>();
        this.createdWolves =         new ArrayList<>();

        new BukkitRunnable() {

            @Override
            public void run() {
                timeCheck();
            }

        }.runTaskTimer(instance, 0L, 20 * 30); // 30 seconds
    }

    // Events
    @EventHandler
    public void onWolfSpawn(final CreatureSpawnEvent event) {

        Entity    entity =    event.getEntity();
        World     world =     entity.getWorld();
        Biome     biome =     entity.getLocation().getBlock().getBiome();
        MoonPhase moonPhase = this.getMoonPhase(world);

        if (world.getTime() < 12000) return;
        if (!configManager.getWorlds().contains(entity.getWorld().getName())) return;
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) return;
        if (entity.getLocation().getY() < configManager.getMinSpawnHeight()) return;

        ArrayList<EntityType> validEntityTypes = new ArrayList<>(Arrays.asList(
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER
        ));

        if (validEntityTypes.contains(entity.getType())) {

            // Prevent mobs from spawn if replace-mobs-completely = true
            event.setCancelled(configManager.isReplaceMobsCompletely());

            if (configManager.isFullMoonOnly() && !moonPhase.equals(MoonPhase.FULL_MOON)) return;

            // Spawn wolves only in biomes from config. During full moon wolves ignore where to spawn.
            if (
                    !moonPhase.equals(MoonPhase.FULL_MOON) &&
                    !configManager.getBiomes().contains(biome.getKey().toString())
            ) return;

            this.spawnWolfpack(entity.getLocation(), event);

        }
    }

    @EventHandler
    public void onWolfDeath(final EntityDeathEvent event) {

        // Get entity and lootables hashmap
        Entity entity = event.getEntity();
        String entityName = "";
        HashMap<String, JSONLootable[]> loot = this.configManager.getWolfCustomLoot();

        if (this.hasAggressiveWolfKey(entity)) {

            event.getDrops().clear();
            switch (entity.getType()) {
                case ZOMBIE -> entityName = "werewolf";
                case WOLF ->   entityName = "wolf";
            }

            for (JSONLootable item : loot.get(entityName)) {

                Boolean drop = Chance.fromPercent(item.getChance());

                if (drop) {
                    event.getDrops().add(item.getItemStack());
                }

            }
        }
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {

        Entity[] entities = event.getChunk().getEntities();

        for (Entity entity : entities) {
            if (entity instanceof Wolf || entity instanceof Zombie) {

                if (!createdWolves.contains(entity) && this.hasAggressiveWolfKey(entity)) {
                    entity.remove();
                }

            }
        }
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {

        Entity entity =  event.getEntity();
        Entity damager = event.getDamager();

        if (entity instanceof Player player) {
            if (!this.hasAggressiveWolfKey(damager)) return;

            Location location = player.getLocation();
            World    world =    player.getWorld();

            player.addPotionEffect(new PotionEffect(
                   PotionEffectType.SLOW,
                   20 * 30, 1,
                   false, false)
            );

            world.spawnParticle(
                  Particle.BLOCK_CRACK, location.clone().add(0, 0.5, 0),
                  12, 0.3, 0.3, 0.3, 0.1,
                  Material.REDSTONE_BLOCK.createBlockData()
            );
            world.playSound(location, Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            if (event.getFinalDamage() > player.getHealth()) playersKilledByWolves.add(player);

        }

    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {

        Player player = event.getEntity();

        if (playersKilledByWolves.contains(player)) {

            event.setDeathMessage(
                    messageManager.getDeathMessage() // Send night-message with formatting
                    .formatted(player.getName())     // that changes %s to moon phase.
            );

            playersKilledByWolves.remove(player);

        }
    }

    // Methods
    private void timeCheck() {

        // Converts string-worlds from config into World object
        List<World> worlds =     this.configManager.getWorlds().stream().map(
                    worldName -> instance.getServer().getWorld(worldName)).toList();

        // Loop through worlds
        worlds.forEach(world -> {

            MoonPhase moonPhase = getMoonPhase(world);

            // Get world time (13000 = night)
            if (world.getTime() >= 13000) {

                // Check if worldTimeSwitch already contains world and it's night
                if (worldTimeSwitch.containsKey(world) && worldTimeSwitch.get(world)) return;

                worldTimeSwitch.put(world, true); // Put world to worldTimeSwitch hashmap

                // Check if fullMoonOnly enabled to announce only full-moon nights
                if (this.configManager.isFullMoonOnly() && !this.getMoonPhase(world).equals(MoonPhase.FULL_MOON)) return;

                // Loop through players
                world.getPlayers().forEach(player -> {

                    player.sendMessage(
                            moonPhase.equals(MoonPhase.FULL_MOON) &&
                            messageManager.isFullMoonNightMessageEnabled()

                                    // Send night announce from config
                                    // %s = to moon phase.

                                    ? messageManager.getFullMoonNightMessage()
                                    .formatted(messageManager.getMoonPhaseNames().get(moonPhase.getNumber()))

                                    : messageManager.getNightMessage()
                                    .formatted(messageManager.getMoonPhaseNames().get(moonPhase.getNumber()))
                    );

                    // Sounds
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1f, 0.7f);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1f);

                });

            }
            else {

                // Check if worldTimeSwitch already contains world and it's day
                if (worldTimeSwitch.containsKey(world) && !worldTimeSwitch.get(world)) return;

                // Put world in worldTimeSwitch with false value (day)
                worldTimeSwitch.put(world, false);

                // Loop through them to remove
                try {
                    createdWolves.forEach(entity -> {

                        if (entity.isDead()) return;
                        if (!this.hasAggressiveWolfKey(entity)) return;

                        world.spawnParticle(
                                Particle.CLOUD, entity.getLocation(),
                                6, 0, 0, 0, 0.1
                        );

                        entity.remove();

                    });
                } finally {
                    createdWolves.clear();
                }
            }
        });
    }

    private void spawnWolfpack(final Location location, final CreatureSpawnEvent event) { // Wolf pack spawn method

        if (configManager.getWolfpackSpawnChance() == 0) return;

        World     world =         location.getWorld();
        MoonPhase moonPhase =     this.getMoonPhase(world);

        Boolean   wolfSpawn =     Chance.fromPercent(configManager.getWolfpackSpawnChance());

        int maxWolvesPerPack =    this.configManager.getMaxWolvesPerPack();

        // Spawn wolves, location cloning due to location-object mutability
        for (int i = 0; i < this.configManager.getMaxWolvesPerPack(); i++) {

            Boolean werewolfSpawn = Chance.fromPercent(this.getWerewolfSpawnChance(moonPhase));

            Location wolfLocation = location.clone().add(
                    (int) (Math.random() * maxWolvesPerPack),
                    0,
                    (int) (Math.random() * maxWolvesPerPack)
            );

            if (wolfLocation.getBlock().getType().isSolid()) return;

            if (wolfSpawn) {

                event.setCancelled(true);

                if (werewolfSpawn) {
                    new WerewolfEntity(wolfLocation, this.createdWolves);
                } else {
                    new WolfEntity(wolfLocation, this.createdWolves);
                }

            }
        }

    }

    private int getWerewolfSpawnChance(MoonPhase moonPhase) {

        int werewolfSpawnChance; // Werewolf spawn chance

        // Moon phase dependant spawn chance
        switch (moonPhase) {

            default -> werewolfSpawnChance = 0;

            case LAST_QUARTER, FIRST_QUARTER ->    werewolfSpawnChance = 5;
            case WANING_GIBBOUS, WAXING_GIBBOUS -> werewolfSpawnChance = 10;
            case FULL_MOON ->                      werewolfSpawnChance = 35;

        }
        return werewolfSpawnChance;
    }

    private Boolean hasAggressiveWolfKey(Entity entity) {
        return entity.getPersistentDataContainer().has(instance.getAggressiveWolvesKey());
    }

    private MoonPhase getMoonPhase(final World world) { // Moon phase getter
        long day = instance.getServer().getWorld(world.getName()).getFullTime() / 24000;
        int phase = (int) (day % 8);

        return MoonPhase.valueOf(phase).get();
    }

    @Getter
    private enum MoonPhase {

        FULL_MOON(0),
        WANING_GIBBOUS(1),
        LAST_QUARTER(2),
        WANING_CRESCENT(3),
        NEW_MOON(4),
        WAXING_CRESCENT(5),
        FIRST_QUARTER(6),
        WAXING_GIBBOUS(7);

        private final int number;

        MoonPhase(int number) {
            this.number = number;
        }

        public static Optional<MoonPhase> valueOf(int value) {
            return Arrays.stream(values())
                    .filter(moonPhase -> moonPhase.getNumber() == value)
                    .findFirst();
        }
    }

}
