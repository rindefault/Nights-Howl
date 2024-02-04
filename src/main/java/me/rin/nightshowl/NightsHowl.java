package me.rin.nightshowl;

import lombok.Getter;
import me.nologic.minority.MinorityExtension;
import me.rin.nightshowl.util.CommandProvider;
import me.rin.nightshowl.util.MessageManager;
import org.bukkit.NamespacedKey;

public final class NightsHowl extends MinorityExtension {

    private CommandProvider   commandProvider;

    @Getter
    private NamespacedKey     aggressiveWolvesKey;

    @Getter
    private static NightsHowl instance;

    @Getter
    private MessageManager    messageManager;

    @Override
    public void onEnable() {

        instance = this;
        messageManager = new MessageManager(this);
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