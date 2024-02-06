package me.rin.nightshowl.utils;

import lombok.Getter;
import me.nologic.minority.MinorityExtension;
import me.nologic.minority.MinorityFeature;
import me.nologic.minority.annotations.Configurable;
import me.nologic.minority.annotations.ConfigurationKey;
import me.nologic.minority.annotations.Type;
import org.bukkit.Bukkit;

import java.util.ArrayList;

@Getter
@Configurable(file = "messages.yml", path = "messages",
        comment = "Leave blank to disable message.")
public class MessageManager implements MinorityFeature {

    @ConfigurationKey(name = "enable-message",
            value = "#FFDA88\uD83C\uDF20 #67C487Night's #5B81A7Howl &8» &7Successfully enabled!")
    private String enableMessage;

    @ConfigurationKey(name = "config-reload-message",
            value = "#FFDA88\uD83C\uDF20 #67C487Night's #5B81A7Howl &8» &7Config reloaded!")
    private String configReloadMessage;

    @ConfigurationKey(name = "night-messages", value = {

            "&7&l%s&r&7... It's better not to go into the woods today.",
            "&7&l%s&r&7... Did you hear that? Was it a howl or a scream?.."},

            type = Type.LIST_OF_STRINGS
    )
    private ArrayList<String> nightMessage;

    @ConfigurationKey(name = "full-moon-night-messages.list", value = {

            "&7It's &l%s&r&7! Night is calling, be aware..." },

            comment = "If disabled, full moon nights will use the same announce as the regular nights.",
            type = Type.LIST_OF_STRINGS
    )
    private ArrayList<String> fullMoonNightMessage;

    @ConfigurationKey(name = "full-moon-night-messages.enabled", value = "true", type = Type.BOOLEAN)
    private boolean fullMoonNightMessageEnabled;

    @ConfigurationKey(name = "death-message", value = {

            "%s was eaten by a wolf", "%s fed himself to the wolves",
            "%s was beheaded by a wolf", "%s didn't notice a pack of wolves" },

            type = Type.LIST_OF_STRINGS
    )
    private ArrayList<String> deathMessage;

    @ConfigurationKey(name = "moon-phase-names", value = {

            "Full Moon", "Waning Gibbous", "Last Quarter",
            "Waning Crescent", "New Moon", "Waxing Crescent",
            "First Quarter", "Waxing Gibbous" },

            type = Type.LIST_OF_STRINGS
    )
    private ArrayList<String> moonPhaseNames;

    public MessageManager(final MinorityExtension plugin) {
        plugin.getConfigurationWizard().generate(this.getClass());
        this.init(this, this.getClass(), plugin);
    }

    public String getFullMoonNightMessage() {
        return fullMoonNightMessage.get((int) (Math.random() * fullMoonNightMessage.size()));
    }

    public String getNightMessage() {
        return nightMessage.get((int) (Math.random() * nightMessage.size()));
    }

    public String getDeathMessage() {
        return deathMessage.get((int) (Math.random() * deathMessage.size()));
    }
}
