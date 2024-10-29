package com.ar.askgaming.realisticeconomy;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.realisticeconomy.datas.SQLiteDatabase;
import com.ar.askgaming.realisticeconomy.listeners.PlayerJoinListener;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{

    private EconomyService economyService;
    private SQLiteDatabase sqlDatabase;

    public void onEnable(){
        
        saveDefaultConfig();

        sqlDatabase = new SQLiteDatabase(this, getDataFolder() + "/economy.db");

        economyService = new EconomyService(this);
        getServer().getServicesManager().register(EconomyService.class, economyService, this, ServicePriority.Highest);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getServer().getPluginCommand("eco").setExecutor(new Commands(this));

        if (setupEconomy()) {
            getLogger().info("EconomyPlugin habilitado y registrado con Vault.");
        } else {
            getLogger().severe("No se encontró Vault. Deshabilitando EconomyPlugin.");
            getServer().getPluginManager().disablePlugin(this);
        }

    }

    public void onDisable() {
        getLogger().info("EconomyPlugin deshabilitado.");
    }

    public EconomyService getEconomyService() {
        return economyService;
    }
    public SQLiteDatabase getSqlDatabase() {
        return sqlDatabase;
    }
        private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        getServer().getServicesManager().register(Economy.class, economyService, this, ServicePriority.Normal);
        return true;
    }
}