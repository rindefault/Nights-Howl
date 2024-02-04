package me.rin.nightshowl.entities;

import me.rin.nightshowl.NightsHowl;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class WolfEntity {

    private final NightsHowl    instance;
    private final NamespacedKey aggressiveWolvesKey;

    public WolfEntity(Location location, ArrayList<Entity> listToStore) {

        // Mob spawn
        Wolf wolf = (Wolf) location.getWorld().spawnEntity(location, EntityType.WOLF);

        instance =            NightsHowl.getInstance();
        aggressiveWolvesKey = instance.getAggressiveWolvesKey();

        // Effects
        ArrayList<PotionEffect> potionEffects = new ArrayList<>(Arrays.asList(
            new PotionEffect(
                PotionEffectType.SPEED,
                -1, 1,
                false, false
            )
        ));

        // Attributes
        wolf.addPotionEffects(potionEffects);

        wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60);
        wolf.setHealth(60);

        wolf.getPersistentDataContainer().set(aggressiveWolvesKey, PersistentDataType.BOOLEAN, true);
        listToStore.add(wolf);

        instance.getServer().getScheduler().runTaskTimer(instance, (task) -> {

            LivingEntity target;

            try {
                target = (LivingEntity) wolf.getNearbyEntities(12, 12, 12).stream().filter(
                              entity -> entity instanceof Player player &&
                                       !player.getGameMode().equals(GameMode.CREATIVE) &&
                                       !player.getGameMode().equals(GameMode.SPECTATOR)
                ).toArray()[0];
            }
            catch (ArrayIndexOutOfBoundsException exception) {
                return;
            }

            wolf.setTarget(target);
            wolf.setAngry(true);

        }, 0L, 120L);
    }
}
