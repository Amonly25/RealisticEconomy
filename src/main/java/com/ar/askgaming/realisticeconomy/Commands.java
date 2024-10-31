package com.ar.askgaming.realisticeconomy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;

public class Commands implements TabExecutor{

    private final Main plugin;
    public Commands(Main main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        return List.of("balance","add","take");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        String lang = "en";
        if (sender instanceof Player){
            Player p = (Player) sender;
            lang = p.getLocale();
        }

        if (args.length == 0){
            sender.sendMessage("Error, use: /eco <balance|server|add|pay|take>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance":
                handleBalanceCommand(sender, args,lang);
                break;
            case "setup":
                handleSetupCommand(sender, args);
                break;
            case "server":
                handleServerCommand(sender, args);
                break;
            case "add":
                if (sender.hasPermission("eco.admin")){
                    handleAddCommand(sender, args);
                } else {
                    sender.sendMessage(plugin.getLang().getFrom("commands.no_perm", lang));
                }
                break;
            case "pay":
                handlePayCommand(sender, args,lang);
                break;
            case "take":
                if (sender.hasPermission("eco.admin")){
                    handleTakeCommand(sender, args);
                } else {
                    sender.sendMessage(plugin.getLang().getFrom("commands.no_perm", lang));
                }
                break;
            case "check":
                if (args.length == 2){
                    Player p = plugin.getServer().getPlayer(args[1]);
                    if (p != null){
                        sender.sendMessage(p.getUniqueId().toString());
                    } else {
                        @SuppressWarnings("deprecation")
                        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(args[1]);
                        sender.sendMessage(offPlayer.getUniqueId().toString());
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }
    private void handlePayCommand(CommandSender sender, String[] args, String lang) {
        if (!(sender instanceof Player)){
            sender.sendMessage("This command can only be executed by a player.");
            return;
        }
        if (args.length != 3){
            sender.sendMessage("Error, use: /eco pay <player> <amount>");
            return;
        }
        Player p = (Player) sender;
        try {
            Double d = Double.valueOf(args[2]);

            EconomyResponse e = plugin.getEconomyService().playerPayPlayer(p, args[1], d);
            if (e.transactionSuccess()){
                p.sendMessage(plugin.getLang().getFrom("transactions.pay", lang).replace("%player%", args[1]).replace("%amount%", args[2]).replace("%balance%", String.valueOf(e.balance)));
            } else {
                p.sendMessage(e.errorMessage);
            }

        } catch (Exception e) {
            p.sendMessage("Error: Invalid Number");
        }
        
    }

    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            try {
                double d = Double.valueOf(args[1]);
                if (sender instanceof ConsoleCommandSender){
                    // revisar si ya ha sido configurado
                    plugin.getServerBank().setup(d);

                } else {
                    sender.sendMessage("This command can only be executed by the console.");
                }
            } catch (Exception e) {
                sender.sendMessage("Invalid Number");
            }
        } else {
            sender.sendMessage("Error, use: /eco setup <quantity>");
        }
    }

    public void handleBalanceCommand(CommandSender sender, String[] args,String lang){
        if (args.length == 1){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.sendMessage(plugin.getLang().getFrom("balance", lang).replace("%balance%", String.valueOf(plugin.getEconomyService().getBalance(p))));
            } else {
                sender.sendMessage(plugin.getLang().getFrom("server_balance", lang).replace("%balance%", String.valueOf(plugin.getServerBank().getBalance())));
            }
        } else if (args.length == 2){
            double balance = plugin.getEconomyService().getBalance(args[1]);
            sender.sendMessage(plugin.getLang().getFrom("balance_other", lang).replace("%balance%", String.valueOf(balance)).replace("%player%", args[1]));
        }
    }
   
    public void handleAddCommand(CommandSender sender, String[] args){
        if (args.length == 3){
            try {
                double d = Double.valueOf(args[2]);
                EconomyResponse bank = plugin.getServerBank().withdraw(d);
                sender.sendMessage(bank.errorMessage);

                if (bank.transactionSuccess()){
                    EconomyResponse e = plugin.getEconomyService().depositPlayer(args[1], d);
                    sender.sendMessage(e.errorMessage);
                }
    
            } catch (Exception e) {
                sender.sendMessage("Error: Invalid Number");
            } 
        } else {
            sender.sendMessage("Error, use: /eco add <player> <amount>");
        }
    }
    public void handleTakeCommand(CommandSender sender, String[] args){
        if (args.length == 3){
            try {
                EconomyResponse e = plugin.getEconomyService().withdrawPlayer(args[1], Double.valueOf(args[2]));
                sender.sendMessage(e.errorMessage);
                if (e.transactionSuccess()){
                    EconomyResponse bank = plugin.getServerBank().deposit(e.amount);
                    sender.sendMessage(bank.errorMessage);
                } 
            } catch (Exception e) {
                sender.sendMessage("Error: Invalid Number.");
            }
        } else {
            sender.sendMessage("Error: use: /eco take <player> <amount>");
        }
    }
    public void handleServerCommand(CommandSender sender, String[] args){
        double d = plugin.getServerBank().getBalance();
        sender.sendMessage(plugin.getLang().getFrom("server_balance", "en").replace("%balance%", String.valueOf(d)));
    }
}
