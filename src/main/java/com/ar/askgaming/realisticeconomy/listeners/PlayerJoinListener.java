package com.ar.askgaming.realisticeconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class PlayerJoinListener implements Listener{

    private RealisticEconomy plugin;

    public PlayerJoinListener(RealisticEconomy main) {
        plugin = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabase().loadPlayerData(player.getUniqueId());
        
    }
}
