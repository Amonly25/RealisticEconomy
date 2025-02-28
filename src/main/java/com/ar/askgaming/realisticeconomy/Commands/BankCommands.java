package com.ar.askgaming.realisticeconomy.Commands;

import java.util.List;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;
import com.ar.askgaming.realisticeconomy.Economy.EconomyManager;

public class BankCommands implements TabExecutor{

    private RealisticEconomy plugin;
    public BankCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("loan", "pay", "deposit", "withdraw", "info");
        } 
        return null;
    }
    private String getLang(String path, Player p){
        return plugin.getLang().getFrom(path, p);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /bank <loan/deposit/withdraw/info>");
            return true;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return true;
        }

        Player p = (Player) sender;
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());

        if (pd == null) {
            p.sendMessage("§cPlayer data not found.");
            return true;
        }
        double amount;

        switch (args[0].toLowerCase()) {
            case "info":
                showBankInfo(p, pd);
                break;
    
            case "loan":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank loan <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                amount = EconomyManager.formatDouble(amount);
                takeLoan(p, pd, amount);
                break;
    
            case "pay":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank pay <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                amount = EconomyManager.formatDouble(amount);
                payDebt(p, pd, amount);
                break;
    
            case "deposit":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank deposit <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                amount = EconomyManager.formatDouble(amount);
                depositToBank(p, pd, amount);
                break;
    
            case "withdraw":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank withdraw <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                amount = EconomyManager.formatDouble(amount);
                withdrawFromBank(p, pd, amount);
                break;
    
            default:
                p.sendMessage("§cInvalid subcommand");
                break;
        }
    
        return true;
    }
    //#region info
    private void showBankInfo(Player p, PlayerData pd) {
        p.sendMessage(getLang("bank.info.interest", p).replace("{value}", String.valueOf(plugin.getEconomyManager().getLoanInterest())));
        p.sendMessage(getLang("bank.info.savings_interest", p).replace("{value}", String.valueOf(plugin.getEconomyManager().getSavingsInterest())));
        p.sendMessage("");
        
        double savingsInterest = pd.getBankBalance() * plugin.getEconomyManager().getSavingsInterest() / 100;

        String formattedNumber = String.format("%.4f", savingsInterest);
        p.sendMessage(getLang("bank.info.balance", p).replace("{value}", String.valueOf(pd.getBankBalance())).replace("{interest}", formattedNumber));
        
        double loanInterest = pd.getDebt() * plugin.getEconomyManager().getLoanInterest() / 100;
        String formattedNumber2 = String.format("%.4f", loanInterest);
        
        p.sendMessage(getLang("bank.info.debt", p).replace("{value}", String.valueOf(pd.getDebt())).replace("{interest}", formattedNumber2));

    }
    //#region loan
    private void takeLoan(Player p, PlayerData pd, double amount) {
        if (amount <= 0) {
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        double limit = plugin.getEconomyManager().getDebtLimit();
        if (pd.getDebt() >= limit || amount > limit - pd.getDebt()) {
            p.sendMessage(getLang("bank.debt_limit", p));
            return;
        }
        int playtime = plugin.getConfig().getInt("playtime_minimum_for_loan",6);
        int total_minutes = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
        int hours = total_minutes / 60;
        if (hours < playtime){
            p.sendMessage(getLang("bank.playtime", p).replace("{hours}", playtime+""));
            return;

        }

        if (plugin.getServerBank().withdrawFromServerToPlayer(p.getUniqueId(), amount)) {
            pd.setDebt(pd.getDebt() + amount);
            pd.save();
            plugin.getEconomyLogger().log("Player " + p.getName() + " took a loan of " + amount);
            p.sendMessage(getLang("bank.loan", p).replace("{amount}", String.valueOf(amount)));
        } else {
            p.sendMessage(getLang("error.transaction", p));
           // plugin.getEconomyLogger().log("Player " + p.getName() + " tried to take a loan of " + amount + " but the transaction failed");
        }
    }
    //#region pay
    private void payDebt(Player p, PlayerData pd, double amount) {

        if (amount <= 0 || amount > pd.getBalance() || amount > pd.getDebt()) {
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        if (plugin.getServerBank().depositFromPlayerToServer(p.getUniqueId(), amount)) {
            pd.setDebt(pd.getDebt() - amount);
            plugin.getEconomyLogger().log("Player " + p.getName() + " paid " + amount + " of debt");
            p.sendMessage(getLang("bank.pay_debt", p).replace("{amount}", String.valueOf(amount)));

            if (pd.isSeizedAccount()){
                if (pd.getDebt() < plugin.getConfig().getDouble("min_debt_to_remove_seized_when_pay", 4000)) {
                    pd.setSeized_account(false);
                    p.sendMessage(getLang("bank.unseized_account", p));
                }
            }
            pd.save();
        } else {
            p.sendMessage(getLang("error.transaction", p));
            //plugin.getEconomyLogger().log("Player " + p.getName() + " tried to pay " + amount + " of debt but the transaction failed");
        }
    }
    //#region deposit
    private void depositToBank(Player p, PlayerData pd, double amount) {
        if (pd.isSeizedAccount()) {
            p.sendMessage(getLang("bank.seized_account", p));
            return;
        }
        if (amount <= 0 || amount > pd.getBalance()) {
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        if (plugin.getEconomyService().depositToPlayerBank(p.getUniqueId(), amount)) {
            p.sendMessage(getLang("bank.deposit", p).replace("{amount}", String.valueOf(amount)));
            plugin.getEconomyLogger().log("Player " + p.getName() + " deposited " + amount + " to the bank");
        } else {
            p.sendMessage(getLang("error.transaction", p));
           // plugin.getEconomyLogger().log("Player " + p.getName() + " tried to deposit " + amount + " to the bank but the transaction failed");

        }
    }
    //#region withdraw
    private void withdrawFromBank(Player p, PlayerData pd, double amount) {
        if (amount <= 0 || amount > pd.getBankBalance()) {
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        if (plugin.getEconomyService().withdrawFromPlayerBank(p.getUniqueId(), amount)) {
            p.sendMessage(getLang("bank.withdraw", p).replace("{amount}", String.valueOf(amount)));
            plugin.getEconomyLogger().log("Player " + p.getName() + " withdrew " + amount + " from the bank");
        } else {
            p.sendMessage(getLang("error.transaction", p));
           // plugin.getEconomyLogger().log("Player " + p.getName() + " tried to withdraw " + amount + " from the bank but the transaction failed");
        }
    }
    
    private boolean isValidAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}