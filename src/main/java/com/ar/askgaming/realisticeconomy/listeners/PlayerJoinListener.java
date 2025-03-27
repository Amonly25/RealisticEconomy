package com.ar.askgaming.realisticeconomy.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class PlayerJoinListener implements Listener{

    private final RealisticEconomy plugin;

    public PlayerJoinListener() {
        plugin = RealisticEconomy.getInstance();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabase().loadPlayerData(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getTimeManager().checkPlayer(player);
            }
        }, 20L);
    }
}
