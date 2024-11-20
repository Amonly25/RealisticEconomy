package com.ar.askgaming.realisticeconomy.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class BankCommands implements TabExecutor{

    private RealisticEconomy plugin;
    public BankCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("loan", "pay", "deposit", "withdraw", "info");
        } else {
            return List.of();
        }
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
                takeLoan(p, pd, amount);
                break;
    
            case "pay":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank pay <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                payDebt(p, pd, amount);
                break;
    
            case "deposit":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank deposit <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
                depositToBank(p, pd, amount);
                break;
    
            case "withdraw":
                if (args.length < 2 || !isValidAmount(args[1])) {
                    p.sendMessage("§cUsage: /bank withdraw <amount>");
                    return true;
                }
                amount = Double.parseDouble(args[1]);
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
        p.sendMessage("Inflacion: " + plugin.getEconomy().getInflation() + "%");
        p.sendMessage("Tasa de interes: " + plugin.getEconomy().getLoanInterest() + "%");
        p.sendMessage("Tasa de interes de Ahorro: " + plugin.getEconomy().getSavingsInterest() + "%");
        p.sendMessage("");
    
        double savingsInterest = pd.getBalance() * plugin.getEconomy().getSavingsInterest() / 100;
        p.sendMessage("Bank balance: " + pd.getBankBalance() + " (Generas " + savingsInterest + " de interes por dia)");
    
        double loanInterest = pd.getDebt() * plugin.getEconomy().getLoanInterest() / 100;
        String extra = pd.getDebt() > 0 ? " (Acumula " + loanInterest + " de interes por dia)" : "";
        p.sendMessage("Deuda: " + pd.getDebt() + extra);
    }
    //#region loan
    private void takeLoan(Player p, PlayerData pd, double amount) {
        if (amount <= 0) {
            p.sendMessage("§cInvalid amount");
            return;
        }
        if (pd.getDebt() >= plugin.getEconomy().getDebtLimit()) {
            p.sendMessage("§cYou can't take more debt");
            return;
        }
        if (plugin.getServerBank().withdrawFromServerToPlayer(p.getUniqueId(), amount)) {
            pd.setDebt(pd.getDebt() + amount);
            pd.save();
            p.sendMessage("§aYou took a loan of " + amount);
        } else {
            p.sendMessage(plugin.getLang().getFrom("error.transaction", p.getLocale()));
        }
    }
    //#region pay
    private void payDebt(Player p, PlayerData pd, double amount) {
        if (amount <= 0 || amount > pd.getBalance() || amount > pd.getDebt()) {
            p.sendMessage("§cInvalid amount");
            return;
        }
        if (plugin.getServerBank().depositFromPlayerToServer(p.getUniqueId(), amount)) {
            pd.setDebt(pd.getDebt() - amount);
            pd.save();
            p.sendMessage("§aYou paid " + amount + " of your debt");
        } else {
            p.sendMessage(plugin.getLang().getFrom("error.transaction", p.getLocale()));
        }
    }
    //#region deposit
    private void depositToBank(Player p, PlayerData pd, double amount) {
        if (amount <= 0 || amount > pd.getBalance()) {
            p.sendMessage("§cInvalid amount");
            return;
        }
        if (plugin.getEconomyService().depositToPlayerBank(p.getUniqueId(), amount)) {
            p.sendMessage("§aYou deposited " + amount + " to your bank account");
        } else {
            p.sendMessage(plugin.getLang().getFrom("error.transaction", p.getLocale()));
        }
    }
    //#region withdraw
    private void withdrawFromBank(Player p, PlayerData pd, double amount) {
        if (amount <= 0 || amount > pd.getBankBalance()) {
            p.sendMessage("§cInvalid amount");
            return;
        }
        if (plugin.getEconomyService().withdrawFromPlayerBank(p.getUniqueId(), amount)) {
            p.sendMessage("§aYou withdrew " + amount + " from your bank account");
        } else {
            p.sendMessage(plugin.getLang().getFrom("error.transaction", p.getLocale()));
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