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
                p.sendMessage(plugin.getLang().getFrom("tokens", p.getLocale()).replace("{amount}", String.valueOf(pd.getTokens())));
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
            p.sendMessage(plugin.getLang().getFrom("error.player_not_found",p.getLocale()));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (Exception e) {
            p.sendMessage(plugin.getLang().getFrom("error.invalid_amount",p.getLocale()));
            return;
        }
        if (amount <= 0){
            p.sendMessage(plugin.getLang().getFrom("error.invalid_amount",p.getLocale()));
            return;
        }
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (amount >= pd.getTokens()){
            p.sendMessage(plugin.getLang().getFrom("error.not_enough",p.getLocale()));
            return;
        }

        if (plugin.getEconomyService().playerPayTokenToPlayer(p.getUniqueId(), target.getUniqueId(), amount)){
            plugin.getEconomyLogger().log("Player " + p.getName() + " paid " + amount + " tokens to " + target.getName());
            p.sendMessage(plugin.getLang().getFrom("token.pay", p.getLocale()).replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)));
            target.sendMessage(plugin.getLang().getFrom("token.receive", p.getLocale()).replace("{player}", p.getName()).replace("{amount}", String.valueOf(amount)));
        } else {
            p.sendMessage(plugin.getLang().getFrom("error.transaction",p.getLocale()));
        }

    }
    private void openShop(Player p){
        p.sendMessage("En desarrollo");
        return;

    }
}
