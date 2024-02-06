package me.rin.nightshowl;

import lombok.Getter;
import me.nologic.minority.MinorityExtension;
import me.rin.nightshowl.utils.CommandProvider;
import me.rin.nightshowl.utils.MessageManager;
import me.rin.nightshowl.utils.WolvesConfigManager;
import org.bukkit.NamespacedKey;

public final class NightsHowl extends MinorityExtension {

    private CommandProvider     commandProvider;

    @Getter
    private NamespacedKey       aggressiveWolvesKey;

    @Getter
    private static NightsHowl   instance;

    @Getter
    private MessageManager      messageManager;

    @Getter
    private WolvesConfigManager configManager;

    @Override
    public void onEnable() {

        instance = this;
        messageManager =      new MessageManager(this);
        configManager =       new WolvesConfigManager(this);
        aggressiveWolvesKey = new NamespacedKey(this, "agressive-wolf");

        this.initializeProviders();

        WolvesBehaviorHandler wolvesBehaviorHandler = new WolvesBehaviorHandler(this);
        super.getServer().getPluginManager().registerEvents(wolvesBehaviorHandler, this);

        super.getServer().getConsoleSender().sendMessage(messageManager.getEnableMessage());
    }

    private void initializeProviders() {

        this.commandProvider = new CommandProvider(this);

    }

}