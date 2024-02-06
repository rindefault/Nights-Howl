package me.rin.nightshowl.entities;

import lombok.Getter;
import me.rin.nightshowl.NightsHowl;
import me.rin.nightshowl.utils.WolvesConfigManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class WerewolfEntity {

    @Getter
    private final Zombie entity;

    private final NightsHowl instance;

    public WerewolfEntity(Location location, ArrayList<Entity> listToStore) {

        // Mob spawn
        this.entity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        instance = NightsHowl.getInstance();

        WolvesConfigManager configManager =       instance.getConfigManager();
        EntityEquipment     equipment =           this.entity.getEquipment();
        NamespacedKey       aggressiveWolvesKey = instance.getAggressiveWolvesKey();

        // Armor
        PlayerProfile profile = this.getProfile();

        ItemStack chestplate =  new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings =    new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots =       new ItemStack(Material.LEATHER_BOOTS);
        ItemStack head =        new ItemStack(Material.PLAYER_HEAD);

        SkullMeta skullMeta =   (SkullMeta) head.getItemMeta();

        skullMeta.setOwnerProfile(profile);
        head.setItemMeta(skullMeta);

        ItemStack[] armor = {chestplate, leggings, boots};
        for (ItemStack armorPart : armor) {

            LeatherArmorMeta armorMeta = (LeatherArmorMeta) armorPart.getItemMeta();

            armorMeta.setColor(Color.fromRGB(195, 195, 195));
            armorPart.setItemMeta(armorMeta);
        }

        equipment.setHelmet(head);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);

        // Attributes
        int maxHealth = configManager.getWerewolfHealthAttribute();
        double damage = configManager.getWerewolfDamageAttribute();
        double speed  = configManager.getWerewolfSpeedAttribute();

        this.entity.setSilent(true);
        this.entity.setCanPickupItems(false);

        this.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        this.entity.setHealth(maxHealth);

        this.entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        this.entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        this.entity.getPersistentDataContainer().set(aggressiveWolvesKey, PersistentDataType.BOOLEAN, true);
        listToStore.add(this.entity);

    }

    // Player profile creation, implementation from mfnalex
    private PlayerProfile getProfile() {

        PlayerProfile  profile  = instance.getServer().createPlayerProfile(UUID.randomUUID()); // Get a new player profile
        PlayerTextures textures = profile.getTextures();

        URL urlObject;

        try {
            urlObject = URI.create("http://textures.minecraft.net/texture/3f65d91e7f0abe46c26f27efbc754ab27b7917ee5c888c1747d882d81c1a13e9").toURL(); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        }
        catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }

        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile

        return profile;
    }
}
