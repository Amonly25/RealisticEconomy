package com.ar.askgaming.realisticeconomy.listeners;

import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ar.askgaming.realisticeconomy.EconomyService;
import com.ar.askgaming.realisticeconomy.Main;

public class PlayerJoinListener implements Listener{

    private final Main plugin;

    private final EconomyService economyService;

    public PlayerJoinListener(Main main) {
        plugin = main;
        economyService = plugin.getEconomyService();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!economyService.hasAccount(player.getName())) {
            plugin.getLogger().info(event.getPlayer() + " no tiene cuenta.");
            economyService.createPlayerAccount(player.getName());
        } else {
            plugin.getLogger().info(event.getPlayer() + " si tiene cuenta.");
        }
    }
}
