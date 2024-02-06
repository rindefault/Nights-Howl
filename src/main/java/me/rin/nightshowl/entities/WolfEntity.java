package me.rin.nightshowl.entities;

import me.rin.nightshowl.NightsHowl;
import me.rin.nightshowl.utils.WolvesConfigManager;
import me.rin.nightshowl.utils.statics.Chance;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class WolfEntity {

    Wolf wolf;

    public WolfEntity(Location location, ArrayList<Entity> listToStore) {

        // Mob spawn
        this.wolf = (Wolf) location.getWorld().spawnEntity(location, EntityType.WOLF);

        NightsHowl          instance =            NightsHowl.getInstance();
        WolvesConfigManager configManager =       instance.getConfigManager();
        NamespacedKey       aggressiveWolvesKey = instance.getAggressiveWolvesKey();

        ArrayList<BukkitTask> tasks = new ArrayList<>();

        // Attributes
        int maxHealth = configManager.getWolfHealthAttribute();
        double damage = configManager.getWolfDamageAttribute();
        double speed =  configManager.getWolfSpeedAttribute();

        wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        wolf.setHealth(maxHealth);

        wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        wolf.getPersistentDataContainer().set(aggressiveWolvesKey, PersistentDataType.BOOLEAN, true);
        listToStore.add(wolf);

        // Tasks

        BukkitTask howlTask = new BukkitRunnable() { // Try to play #howl() every 30 seconds
            @Override
            public void run() {
                howl();
            }

        }.runTaskTimer(instance, 0L, 600L);

        BukkitTask assignTargetTask = new BukkitRunnable() { // Target assignment task
            @Override
            public void run() {

                // Also checking if wolf is dead and cancelling all the tasks
                if (wolf.isDead()) {
                    tasks.forEach(BukkitTask::cancel);
                    tasks.clear();
                    return;
                }
                assignTarget();
            }
        }.runTaskTimer(instance, 0L, 60L);

        // Add tasks to taskList
        tasks.addAll(List.of(howlTask, assignTargetTask));

    }

    private void howl() {

        // Randomly play howl sound with 20 percent chance
        if (Chance.fromPercent(20)) {

            this.wolf.getLocation().getWorld().playSound(
                    this.wolf.getLocation(),
                    Sound.ENTITY_WOLF_HOWL,
                    1f, 1f
            );

        }
    }

    private void assignTarget() {

        LivingEntity target;

        // Get nearby players and set them as a target
        try {
            target = (LivingEntity) this.wolf.getNearbyEntities(12, 12, 12).stream().filter(
                    entity -> entity instanceof Player player &&
                            !player.getGameMode().equals(GameMode.CREATIVE) &&
                            !player.getGameMode().equals(GameMode.SPECTATOR)
            ).toArray()[0];
        }
        catch (ArrayIndexOutOfBoundsException exception) {
            return;
        }

        // Removes the target if it's close to light source
        if (target.getLocation().getBlock().getLightFromBlocks() >= 2) {
            if (this.wolf.getTarget() != null && this.wolf.getTarget().equals(target)) {
                this.wolf.setTarget(null);
                this.wolf.setAngry(false);
            }
            return;
        }

        wolf.setTarget(target);
        wolf.setAngry(true);

    }
}
