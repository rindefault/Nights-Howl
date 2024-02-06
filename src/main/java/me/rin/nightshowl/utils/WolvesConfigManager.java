package me.rin.nightshowl.utils;

import org.bukkit.Material;
import org.json.JSONObject;
import lombok.Getter;
import me.nologic.minority.MinorityFeature;
import me.nologic.minority.annotations.Configurable;
import me.nologic.minority.annotations.ConfigurationKey;
import me.nologic.minority.annotations.Type;
import me.rin.nightshowl.NightsHowl;
import me.rin.nightshowl.items.JSONLootable;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Configurable(path = "settings", comment = "Main settings of Night's Howl plugin.")
public class WolvesConfigManager implements MinorityFeature {

    // Main fields init
    private final NightsHowl instance;
    private final HashMap<String, JSONLootable[]> wolfCustomLoot;

    // Configuration keys
    @ConfigurationKey(name = "worlds", type = Type.LIST_OF_STRINGS, value = "world")
    private ArrayList<String> worlds;

    @ConfigurationKey(name = "biomes", type = Type.LIST_OF_STRINGS, value = "minecraft:forest")
    private ArrayList<String> biomes;

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

    @ConfigurationKey(name = "mob-attributes.wolf.health", type = Type.INTEGER, value = "30",
            comment = "Attributes of wolves. Custom loot in JSON, valid materials: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html. Amount range returns the amount from 1 to your number.")
    private int wolfHealthAttribute;

    @ConfigurationKey(name = "mob-attributes.wolf.damage", type = Type.DOUBLE, value = "5.0")
    private double wolfDamageAttribute;

    @ConfigurationKey(name = "mob-attributes.wolf.speed", type = Type.DOUBLE, value = "0.35")
    private double wolfSpeedAttribute;

    @ConfigurationKey(name = "mob-attributes.wolf.custom-loot", type = Type.LIST_OF_STRINGS,
            value = "{Name:\"&r&7Wolf's meat\",Material:\"BEEF\",CustomModelData:0,AmountRange:3,Chance:50}")
    private ArrayList<String> wolfLootAttribute;

    @ConfigurationKey(name = "mob-attributes.werewolf.health", type = Type.INTEGER, value = "60")
    private int werewolfHealthAttribute;

    @ConfigurationKey(name = "mob-attributes.werewolf.damage", type = Type.DOUBLE, value = "6.0")
    private double werewolfDamageAttribute;

    @ConfigurationKey(name = "mob-attributes.werewolf.speed", type = Type.DOUBLE, value = "0.28")
    private double werewolfSpeedAttribute;

    @ConfigurationKey(name = "mob-attributes.werewolf.custom-loot", type = Type.LIST_OF_STRINGS,
            value = "{Name:\"&r&7Wolf's wool\",Material:\"LIGHT_GRAY_WOOL\",CustomModelData:0,AmountRange:1,Chance:25}")
    private ArrayList<String> werewolfLootAttribute;

    public WolvesConfigManager(final NightsHowl instance) {

        this.instance = instance;

        instance.getConfigurationWizard().generate(this.getClass());
        this.init(this, this.getClass(), instance);

        wolfCustomLoot = new HashMap<>();

        JSONLootable[] wolfJSONLootables = wolfLootAttribute.stream().map(this::deserializeJSONItem).toArray(JSONLootable[]::new);
        JSONLootable[] werewolfJSONLootables = werewolfLootAttribute.stream().map(this::deserializeJSONItem).toArray(JSONLootable[]::new);

        wolfCustomLoot.put("werewolf", werewolfJSONLootables);
        wolfCustomLoot.put("wolf", wolfJSONLootables);
    }

    private JSONLootable deserializeJSONItem(String string) {

        JSONObject jsonString = new JSONObject(string);

        String name = jsonString.getString("Name");
        int customModelData = jsonString.getInt("CustomModelData");
        int amountRange = jsonString.getInt("AmountRange");
        int chance = jsonString.getInt("Chance");
        Material material = Material.getMaterial(jsonString.getString("Material"));

        return new JSONLootable(name, material, customModelData, amountRange, chance);

    }

}
