package me.rin.nightshowl.utils;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.rin.nightshowl.NightsHowl;
import org.bukkit.Sound;
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

        NightsHowl     instance =           NightsHowl.getInstance();
        MessageManager messageManager =     instance.getMessageManager();
        WolvesConfigManager configManager = instance.getConfigManager();

        @CommandAlias("reload")
        @CommandPermission("nightshowl.reload")
        private void reload(final Player player) {
            instance.getMessageManager().init(messageManager, messageManager.getClass(), instance);
            instance.getMessageManager().init(configManager, configManager.getClass(), instance);

            player.sendMessage(instance.getMessageManager().getConfigReloadMessage());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        }
    }
}