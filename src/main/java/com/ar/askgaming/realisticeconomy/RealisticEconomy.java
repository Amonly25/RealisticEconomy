package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.block.Vault;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.realisticeconomy.Commands.AuctionCommands;
import com.ar.askgaming.realisticeconomy.Commands.BankCommands;
import com.ar.askgaming.realisticeconomy.Commands.EcoCommands;
import com.ar.askgaming.realisticeconomy.Commands.LoteryCommands;
import com.ar.askgaming.realisticeconomy.Commands.TokenCommands;
import com.ar.askgaming.realisticeconomy.Data.DatabaseManager;
import com.ar.askgaming.realisticeconomy.Data.LangManager;
import com.ar.askgaming.realisticeconomy.Economy.BankTransactions;
import com.ar.askgaming.realisticeconomy.Economy.EconomyLogger;
import com.ar.askgaming.realisticeconomy.Economy.EconomyTransactions;
import com.ar.askgaming.realisticeconomy.Listeners.PlayerJoinListener;
import com.ar.askgaming.realisticeconomy.Utilities.UtilityMethods;
import com.ar.askgaming.realisticeconomy.Utilities.VaultHook;

import net.milkbowl.vault.economy.Economy;

public class RealisticEconomy extends JavaPlugin{

    private String serverLang;

    private EconomyTransactions economyService;
    private BankTransactions serverBank;
    private UtilityMethods utilityMethods;
    private com.ar.askgaming.realisticeconomy.Economy.Economy economy;

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
        economy = new com.ar.askgaming.realisticeconomy.Economy.Economy(this);
        economyService = new EconomyTransactions(this);
        serverBank = new BankTransactions(this);

        serverLang = getConfig().getString("server_lang", "en");
        economyLogger = new EconomyLogger(this);
        utilityMethods = new UtilityMethods(this);
        
        getServer().getServicesManager().register(EconomyTransactions.class, economyService, this, ServicePriority.Highest);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getServer().getPluginCommand("eco").setExecutor(new EcoCommands(this));
        getServer().getPluginCommand("bank").setExecutor(new BankCommands(this));
        getServer().getPluginCommand("lotery").setExecutor(new LoteryCommands(this));
        getServer().getPluginCommand("reauction").setExecutor(new AuctionCommands(this));
        getServer().getPluginCommand("tokens").setExecutor(new TokenCommands(this));

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
    public BankTransactions getServerBank() {
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
    public com.ar.askgaming.realisticeconomy.Economy.Economy getEconomy() {
        return economy;
    }

}