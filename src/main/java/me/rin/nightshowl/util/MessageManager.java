package me.rin.nightshowl.util;

import lombok.Getter;
import me.nologic.minority.MinorityExtension;
import me.nologic.minority.MinorityFeature;
import me.nologic.minority.annotations.Configurable;
import me.nologic.minority.annotations.ConfigurationKey;
import me.nologic.minority.annotations.Type;

import java.util.ArrayList;

@Configurable(file = "messages.yml", path = "messages",
        comment = "Leave blank to disable message.")
public class MessageManager implements MinorityFeature {

    @ConfigurationKey(name = "enable-message",
            value = "#FFDA88\uD83C\uDF20 #67C487Night's #5B81A7Howl &8» &7Successfully enabled!",
            comment = "Leave two blank brackets to disable messages")
    @Getter
    private String enableMessage;

    @ConfigurationKey(name = "config-reload-message",
            value = "#FFDA88\uD83C\uDF20 #67C487Night's #5B81A7Howl &8» &7Config reloaded!")
    @Getter
    private String configReloadMessage;

    @ConfigurationKey(name = "night-message", value = "&7It's &l%s&r&7! Night is calling, be aware...")
    @Getter
    private String nightMessage;

    @ConfigurationKey(name = "death-message", value = "%s was eaten by a wolf")
    @Getter
    private String deathMessage;

    @ConfigurationKey(name = "moon-phase-names", value = {

            "Full Moon", "Waning Gibbous", "Last Quarter",
            "Waning Crescent", "New Moon", "Waxing Crescent",
            "First Quarter", "Waxing Gibbous" },

            type = Type.LIST_OF_STRINGS
    )
    @Getter
    private ArrayList<String> moonPhaseNames;

    public MessageManager(final MinorityExtension plugin) {
        plugin.getConfigurationWizard().generate(this.getClass());
        this.init(this, this.getClass(), plugin);
    }

}
