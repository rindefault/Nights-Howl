package me.rin.nightshowl;

import lombok.Getter;
import me.nologic.minority.MinorityFeature;
import me.nologic.minority.annotations.Configurable;
import me.nologic.minority.annotations.ConfigurationKey;
import me.nologic.minority.annotations.Type;
import me.rin.nightshowl.entities.WerewolfEntity;
import me.rin.nightshowl.entities.WolfEntity;
import me.rin.nightshowl.util.MessageManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Configurable(path = "settings", comment = "Main settings of Night's Howl plugin.")
public class WolvesBehaviorHandler implements MinorityFeature, Listener {

    private final NightsHowl              instance;
    private final MessageManager          messageManager;

    private final ArrayList<Player>       playersKilledByWolves;
    private final HashMap<World, Boolean> worldTimeSwitch;
    private final ArrayList<Entity>       createdWolves;

    // Configuration keys
    @ConfigurationKey(name = "worlds", type = Type.LIST_OF_STRINGS, value = "world")
    private ArrayList<String> worlds;

    @ConfigurationKey(name = "max-wolves-per-pack", type = Type.INTEGER, value = "4")
    private int maxWolvesPerPack;

    @ConfigurationKey(name = "full-moon-only", type = Type.BOOLEAN, value = "false",
            comment = "Enables wolves spawn only when it's full moon. Take in mind, that with replace-mobs-completely enabled mobs will not spawn.")
    private boolean fullMoonOnly;

    @ConfigurationKey(name = "replace-mobs-completely", type = Type.BOOLEAN, value = "true",
            comment = "By default, mobs such as zombies, spiders and skeletons are replaced with wolf packs. With this setting disabled mobs will be able to spawn.")
    private boolean replaceMobsCompletely;

    @ConfigurationKey(name = "wolf-pack-spawn-chance", type = Type.INTEGER, value = "25",
            comment = "Chance of replacing the mob with a wolf pack on spawn. If replace-mobs-completely is not turned off, mobs that have not been replaced will not spawn.")
    private int wolfpackSpawnChance;

    @ConfigurationKey(name = "min-spawn-height", type = Type.INTEGER, value = "64",
            comment = "Minimum height where wolves can spawn.")
    private int minSpawnHeight;

    // Initialization
    public WolvesBehaviorHandler(final NightsHowl plugin) {

        this.instance =              plugin;
        this.messageManager =        plugin.getMessageManager();

        this.playersKilledByWolves = new ArrayList<>();
        this.worldTimeSwitch =       new HashMap<>();
        this.createdWolves =         new ArrayList<>();

        plugin.getConfigurationWizard().generate(this.getClass());
        this.init(this, this.getClass(), instance);

        new BukkitRunnable() {

            @Override
            public void run() {
                timeCheck();
            }

        }.runTaskTimer(instance, 0L, 20 * 30); // 30 seconds
    }

    // Events
    @EventHandler
    public void onMobSpawn(final CreatureSpawnEvent event) {

        Entity    entity =    event.getEntity();
        World     world =     entity.getWorld();
        MoonPhase moonPhase = getMoonPhase(world);

        if (world.getTime() < 12000) return;
        if (!worlds.contains(entity.getWorld().getName())) return;
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) return;
        if (entity.getLocation().getY() < minSpawnHeight) return;

        ArrayList<EntityType> validEntityTypes = new ArrayList<>(Arrays.asList(
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER
        ));

        if (validEntityTypes.contains(entity.getType())) {

            event.setCancelled(replaceMobsCompletely);

            if (fullMoonOnly && !moonPhase.equals(MoonPhase.FULL_MOON)) return;

            Random random = new Random();
            int    chance =     random.nextInt(100 / wolfpackSpawnChance);

            if (chance == 0) {
                event.setCancelled(true);

                this.spawnWolfpack(entity.getLocation());

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
                                                     // that changes %s to moon phase.
                    .formatted(player.getName())
            );

            playersKilledByWolves.remove(player);

        }
    }

    // Methods
    private void timeCheck() {

        // Converts string-worlds from config into World object
        List<World> worlds =     this.worlds.stream().map(
                    worldName -> instance.getServer().getWorld(worldName)).toList();

        // Loop through worlds
        worlds.forEach(world -> {

            MoonPhase moonPhase = getMoonPhase(world);

            // Get world time (12000 = night)
            if (world.getTime() >= 12000) {

                // Check if worldTimeSwitch already contains world and it's night
                if (worldTimeSwitch.containsKey(world) && worldTimeSwitch.get(world)) return;

                worldTimeSwitch.put(world, true); // Put world to worldTimeSwitch hashmap

                // Loop through players
                world.getPlayers().forEach(player -> {

                    // Check if fullMoonOnly enabled to announce only full-moon nights
                    if (this.fullMoonOnly && !this.getMoonPhase(world).equals(MoonPhase.FULL_MOON)) return;

                    player.sendMessage(
                            messageManager.getNightMessage() // Send night-message with formatting
                                                             // that changes %s to moon phase.
                            .formatted(messageManager.getMoonPhaseNames().get(moonPhase.getNumber()))
                    );

                    // Creepy sounds
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 1f, 1f);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1f, 1f);

                });

            }
            else {

                // Check if worldTimeSwitch already contains world and it's day
                if (worldTimeSwitch.containsKey(world) && !worldTimeSwitch.get(world)) return;

                // Put world in worldTimeSwitch with false value (day)
                worldTimeSwitch.put(world, false);

                // Filter only Zombie (Werewolf) and Wolf entity types, loop through them to remove
                world.getEntities().stream().filter(entity ->
                        entity.getType().equals(EntityType.ZOMBIE) ||
                        entity.getType().equals(EntityType.WOLF)
                ).forEach(entity -> {

                    if (!this.hasAggressiveWolfKey(entity)) return;

                    world.spawnParticle(
                            Particle.CLOUD, entity.getLocation(),
                            6, 0, 0, 0, 0.1
                    );

                    entity.remove();
                    createdWolves.remove(entity);

                });
            }
        });
    }

    private void spawnWolfpack(final Location location) { // Wolf pack spawn method

        World     world =     location.getWorld();
        MoonPhase moonPhase = this.getMoonPhase(world);

        int       werewolfSpawnChance; // Werewolf spawn chance

        // Moon phase dependant spawn chance calculation
        switch (moonPhase) {
            case LAST_QUARTER ->   werewolfSpawnChance = this.maxWolvesPerPack - 3;
            case WANING_GIBBOUS -> werewolfSpawnChance = this.maxWolvesPerPack - 2;
            case FULL_MOON ->      werewolfSpawnChance = this.maxWolvesPerPack - 1;

            default ->             werewolfSpawnChance = 0;
        }

        // Spawn wolves, location cloning due to location-object mutability
        for (int i = 0; i < this.maxWolvesPerPack; i++) {

            Location wolfLocation = location.clone().add(
                    (int) (Math.random() * this.maxWolvesPerPack),
                    0,
                    (int) (Math.random() * this.maxWolvesPerPack)
            );

            if(wolfLocation.getBlock().getType().isSolid()) return;

            if ((int) (Math.random() * this.maxWolvesPerPack) == werewolfSpawnChance) {
                new WerewolfEntity(wolfLocation, this.createdWolves);
            }
            new WolfEntity(wolfLocation, this.createdWolves);
        }

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

        FULL_MOON(7),
        WANING_GIBBOUS(6),
        LAST_QUARTER(5),
        WANING_CRESCENT(4),
        NEW_MOON(3),
        WAXING_CRESCENT(2),
        FIRST_QUARTER(1),
        WAXING_GIBBOUS(0);

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