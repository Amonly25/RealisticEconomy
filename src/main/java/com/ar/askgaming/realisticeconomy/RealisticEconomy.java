package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.block.Vault;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.realisticeconomy.datas.DatabaseManager;
import com.ar.askgaming.realisticeconomy.datas.LangManager;
import com.ar.askgaming.realisticeconomy.listeners.PlayerJoinListener;
import com.ar.askgaming.realisticeconomy.utils.EconomyLogger;
import com.ar.askgaming.realisticeconomy.utils.UtilityMethods;
import com.ar.askgaming.realisticeconomy.utils.VaultHook;

import net.milkbowl.vault.economy.Economy;

public class RealisticEconomy extends JavaPlugin{

    private String serverLang;

    private EconomyTransactions economyService;
    private ServerBank serverBank;
    private UtilityMethods utilityMethods;
    private com.ar.askgaming.realisticeconomy.Economy economy;

    private DatabaseManager database;
    private LangManager langHandler;
    private EconomyLogger economyLogger;
    private VaultHook vaultHook;

    public void onEnable(){
        
        saveDefaultConfig();

        database = new DatabaseManager(this);

        try (Connection conn = database.connect()) {
            getLogger().info("Connected to database.");
            database.createTable();
            database.createServerBankTable();
        } catch (SQLException e) {
            getServer().getPluginManager().disablePlugin(this);
            e.printStackTrace();
        }
        
        langHandler = new LangManager(this);
        economy = new com.ar.askgaming.realisticeconomy.Economy(this);
        economyService = new EconomyTransactions(this);
        serverBank = new ServerBank(this);

        serverLang = getConfig().getString("server_lang", "en");
        economyLogger = new EconomyLogger(this);
        utilityMethods = new UtilityMethods(this);
        
        getServer().getServicesManager().register(EconomyTransactions.class, economyService, this, ServicePriority.Highest);

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

    public EconomyTransactions getEconomyService() {
        return economyService;
    }
    public ServerBank getServerBank() {
        return serverBank;
    }
    public DatabaseManager getDatabase() {
        return database;
    }
    public LangManager getLang() {
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
        getServer().getServicesManager().register(Economy.class, vaultHook, this, ServicePriority.Normal);
        return true;
    }
    public UtilityMethods getUtilityMethods() {
        return utilityMethods;
    }
    public com.ar.askgaming.realisticeconomy.Economy getEconomy() {
        return economy;
    }

}