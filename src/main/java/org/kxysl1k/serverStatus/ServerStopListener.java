package org.kxysl1k.serverStatus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class ServerStopListener implements Listener {

    private final ServerStatus plugin;

    public ServerStopListener(ServerStatus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            plugin.setChannelName(plugin.getStatusChannelId(), plugin.getOfflineTitle(), null);
        }
    }
}