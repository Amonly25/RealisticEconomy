package com.ar.askgaming.realisticeconomy.Utilities;

import java.util.List;

import org.bukkit.OfflinePlayer;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultHook implements Economy{

    private RealisticEconomy plugin;

    public VaultHook(RealisticEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public EconomyResponse bankBalance(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'bankDeposit'");
    }

    @Override
    public EconomyResponse bankDeposit(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'bankDeposit'");
    }

    @Override
    public EconomyResponse bankHas(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'bankHas'");
    }

    @Override
    public EconomyResponse bankWithdraw(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'bankWithdraw'");
    }

    @Override
    public EconomyResponse createBank(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }

    @Override
    public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }

    @Override
    public boolean createPlayerAccount(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public String currencyNamePlural() {
        throw new UnsupportedOperationException("Unimplemented method 'currencyNamePlural'");
    }

    @Override
    public String currencyNameSingular() {
        throw new UnsupportedOperationException("Unimplemented method 'currencyNameSingular'");
    }

    @Override
    public EconomyResponse deleteBank(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteBank'");
    }

    @Override
    public EconomyResponse depositPlayer(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer arg0, double arg1) {
        boolean t = plugin.getEconomyService().depositPlayer(arg0.getUniqueId(), arg1);
        double balance = plugin.getDatabase().loadPlayerData(arg0.getUniqueId()).getBalance();
        return new EconomyResponse(arg1, balance, t ? ResponseType.SUCCESS : ResponseType.FAILURE, "");
        
    }

    @Override
    public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public String format(double arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }

    @Override
    public int fractionalDigits() {
        throw new UnsupportedOperationException("Unimplemented method 'fractionalDigits'");
    }

    @Override
    public double getBalance(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public double getBalance(OfflinePlayer arg0) {
        return plugin.getDatabase().loadPlayerData(arg0.getUniqueId()).getBalance();
    }

    @Override
    public double getBalance(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public double getBalance(OfflinePlayer arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public List<String> getBanks() {
        throw new UnsupportedOperationException("Unimplemented method 'getBanks'");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public boolean has(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(OfflinePlayer arg0, double arg1) {
        return plugin.getDatabase().loadPlayerData(arg0.getUniqueId()).getBalance() >= arg1;
    }

    @Override
    public boolean has(String arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean hasAccount(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(OfflinePlayer arg0) {
        if (plugin.getDatabase().loadPlayerData(arg0.getUniqueId()) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasAccount(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(OfflinePlayer arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasBankSupport() {
        return true;
    }

    @Override
    public EconomyResponse isBankMember(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }

    @Override
    public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }

    @Override
    public EconomyResponse isBankOwner(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }

    @Override
    public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public EconomyResponse withdrawPlayer(String arg0, double arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer arg0, double arg1) {
        boolean t = plugin.getEconomyService().withdrawPlayer(arg0.getUniqueId(), arg1);
        double balance = plugin.getDatabase().loadPlayerData(arg0.getUniqueId()).getBalance();
        return new EconomyResponse(arg1, balance, t ? ResponseType.SUCCESS : ResponseType.FAILURE, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }
}
