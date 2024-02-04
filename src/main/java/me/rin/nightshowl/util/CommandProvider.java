package me.rin.nightshowl.util;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.rin.nightshowl.NightsHowl;
import org.bukkit.entity.Player;

public class CommandProvider {

    private final NightsHowl           plugin;
    private final BukkitCommandManager manager;

    public CommandProvider(final NightsHowl plugin) {

        this.plugin =      plugin;
        this.manager = new BukkitCommandManager(this.plugin);

        this.registerCommands();
    }

    private void registerCommands() {
        this.manager.registerCommand(new ReloadCommand());
    }

    @CommandAlias("nighshowl")
    private static class ReloadCommand extends BaseCommand {

        NightsHowl instance = NightsHowl.getInstance();

        @CommandAlias("reload")
        @CommandPermission("nightshowl.reload")
        private void reload(final Player player) {
            instance.reloadConfig();
            player.sendMessage(instance.getMessageManager().getConfigReloadMessage());
        }
    }
}