package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.realisticeconomy.Commands.BankCommands;
import com.ar.askgaming.realisticeconomy.Commands.EcoCommands;
import com.ar.askgaming.realisticeconomy.Commands.TokenCommands;
import com.ar.askgaming.realisticeconomy.Data.DatabaseManager;
import com.ar.askgaming.realisticeconomy.Data.LangManager;
import com.ar.askgaming.realisticeconomy.Economy.BankTransactions;
import com.ar.askgaming.realisticeconomy.Economy.EconomyLogger;
import com.ar.askgaming.realisticeconomy.Economy.EconomyManager;
import com.ar.askgaming.realisticeconomy.Economy.EconomyService;
import com.ar.askgaming.realisticeconomy.Economy.TokenShop;
import com.ar.askgaming.realisticeconomy.Listeners.InventoryClickListener;
import com.ar.askgaming.realisticeconomy.Listeners.PlayerJoinListener;
import com.ar.askgaming.realisticeconomy.Lottery.Lottery;
import com.ar.askgaming.realisticeconomy.Lottery.LotteryManager;
import com.ar.askgaming.realisticeconomy.Utilities.PlaceHolders;
import com.ar.askgaming.realisticeconomy.Utilities.TimeManager;
import com.ar.askgaming.realisticeconomy.Utilities.UtilityMethods;
import com.ar.askgaming.realisticeconomy.Utilities.VaultHook;

import net.milkbowl.vault.economy.Economy;

public class RealisticEconomy extends JavaPlugin{

    private static RealisticEconomy instance;

    private EconomyService economyService;
    private BankTransactions serverBank;
    private UtilityMethods utilityMethods;
    private LotteryManager lotteryManager;
    private EconomyManager economyManager;
    private TimeManager timeManager;
    private DatabaseManager database;
    private LangManager langHandler;
    private EconomyLogger economyLogger;
    private VaultHook vaultHook;
    private TokenShop tokenShop;

    public void onEnable(){
        instance = this;

        saveDefaultConfig();

        database = new DatabaseManager(this);

        ConfigurationSerialization.registerClass(Lottery.class,"Lottery");

        try (Connection conn = database.getConnection()) {
            getLogger().info("Connected to database.");
            database.createTable();
            database.createServerBankTable();
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database. Disabling plugin...");
            getLogger().severe("Error: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        langHandler = new LangManager(this);
        
        economyService = new EconomyService(this);
        serverBank = new BankTransactions(this);
        
        economyManager = new EconomyManager(this);
        lotteryManager = new LotteryManager(this);
        timeManager = new TimeManager(this);

        economyLogger = new EconomyLogger(this);
        utilityMethods = new UtilityMethods(this);
        tokenShop = new TokenShop(this);
        
        getServer().getServicesManager().register(EconomyService.class, economyService, this, ServicePriority.Highest);

        getServer().getPluginCommand("eco").setExecutor(new EcoCommands(this));
        getServer().getPluginCommand("bank").setExecutor(new BankCommands(this));
        getServer().getPluginCommand("tokens").setExecutor(new TokenCommands(this));

        new InventoryClickListener();
        new PlayerJoinListener();

        if (setupVault()) {
            getLogger().info("Hooked into Vault!");
        } else {
            getLogger().severe("Vault not found!");
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceHolders();
        }
    }

    public void onDisable() {
        database.disconnect();
        
    }
    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        vaultHook = new VaultHook(this);
        getServer().getServicesManager().register(Economy.class, vaultHook, this, ServicePriority.Normal);
        return true;
    }
    public EconomyService getEconomyService() {
        return economyService;
    }
    public TokenShop getTokenShop() {
        return tokenShop;
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
    public static RealisticEconomy getInstance() {
        return instance;
    }
    public EconomyLogger getEconomyLogger() {
        return economyLogger;
    }
    public UtilityMethods getUtilityMethods() {
        return utilityMethods;
    }

    public LotteryManager getLoteryManager() {
        return lotteryManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    public TimeManager getTimeManager() {
        return timeManager;
    }

}