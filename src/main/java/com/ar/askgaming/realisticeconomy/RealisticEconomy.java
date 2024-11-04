package com.ar.askgaming.realisticeconomy;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.realisticeconomy.datas.LangHandler;
import com.ar.askgaming.realisticeconomy.datas.SQLiteDatabase;
import com.ar.askgaming.realisticeconomy.listeners.PlayerJoinListener;
import com.ar.askgaming.realisticeconomy.utils.EconomyLogger;

import net.milkbowl.vault.economy.Economy;

public class RealisticEconomy extends JavaPlugin{

    private String serverLang;

    private EconomyService economyService;
    private ServerBank serverBank;

    private SQLiteDatabase sqlDatabase;

    private LangHandler langHandler;
    private EconomyLogger economyLogger;

    public void onEnable(){
        
        saveDefaultConfig();

        sqlDatabase = new SQLiteDatabase(this, getDataFolder() + "/economy.db");
        langHandler = new LangHandler(this);

        economyService = new EconomyService(this);
        serverBank = new ServerBank(this);

        serverLang = getConfig().getString("server_lang", "en");
        economyLogger = new EconomyLogger(this);
        
        getServer().getServicesManager().register(EconomyService.class, economyService, this, ServicePriority.Highest);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getServer().getPluginCommand("eco").setExecutor(new Commands(this));

        if (setupVault()) {
            getLogger().info("EconomyPlugin found and hooked into Vault.");
        } else {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void onDisable() {
        //sqlDatabase.close();
    }

    public EconomyService getEconomyService() {
        return economyService;
    }
    public ServerBank getServerBank() {
        return serverBank;
    }
    public SQLiteDatabase getSqlDatabase() {
        return sqlDatabase;
    }
    public LangHandler getLang() {
        return langHandler;
    }
    public String getServerLang() {
        return serverLang;
    }
    public EconomyLogger getEconomyLogger() {
        return economyLogger;
    }
    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        getServer().getServicesManager().register(Economy.class, economyService, this, ServicePriority.Normal);
        return true;
    }
}