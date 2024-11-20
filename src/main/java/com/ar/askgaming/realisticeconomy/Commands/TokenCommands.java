package com.ar.askgaming.realisticeconomy.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class TokenCommands implements TabExecutor{

    private RealisticEconomy plugin;
    public TokenCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("pay", "shop", "balance");
        } else {
            return List.of();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /token <pay/shop/balance>");
            return true;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return true;

        }
        Player p = (Player) sender;
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        
        switch (args[0].toLowerCase()) {
            case "balance":
                p.sendMessage("Token balance: " + pd.getTokens());
                break;
            case "pay":
                if (args.length < 3) {
                    p.sendMessage("§cUsage: /token pay <player> <amount>");
                    return true;
                }
                pay(p, args);
                break;
            case "shop":
                openShop(p);
                break;
            default:
                sender.sendMessage("§cUsage: /token <pay/shop/balance>");
                break;
        }
        return true;
    }
    private void pay(Player p, String[] args){
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null){
            p.sendMessage("§cPlayer not found");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (Exception e) {
            p.sendMessage("§cInvalid amount");
            return;
        }
        if (amount <= 0){
            p.sendMessage("§cInvalid amount");
            return;
        }
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (amount >= pd.getTokens()){
            p.sendMessage("§cYou don't have enough tokens");
            return;
        }

        if (plugin.getEconomyService().playerPayTokenToPlayer(p.getUniqueId(), target.getUniqueId(), amount)){
            p.sendMessage("§aPayment successful");
            target.sendMessage("§aYou received " + amount + " tokens from " + p.getName());
        } else {
            p.sendMessage("§cPayment failed");
        }

    }
    private void openShop(Player p){
        p.sendMessage("En desarrollo");
        return;

    }
}
