package com.ar.askgaming.realisticeconomy.Lottery;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class LotteryCommands implements TabExecutor {

    private RealisticEconomy plugin;
    public LotteryCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("buy", "info", "draw", "create", "reset", "delete");
        } 
        if (args.length == 2){
            return plugin.getLoteryManager().getLoteryList().keySet().stream().toList();
        }
        
        return null;
    }
    private String getLang(String path, Player p){
        return plugin.getLang().getFrom(path, p);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       if (args.length == 0) {
           sender.sendMessage("Usage: /lottery <buy/info/draw/create/reset/delete>");
           return true;
       }
       if (!(sender instanceof Player)) {
           sender.sendMessage("Only players can use this command.");
           return true;
       }
       Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "buy":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /lottery buy <lotery_name> <number>");
                    return true;
                }
                String loteryName = args[1];
                int number;
                try {
                    number = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number.");
                    return true;
                }
                // Buy ticket
                plugin.getLoteryManager().buyTicket(player, loteryName, number);

                break;
            case "reset":
                 if (!player.hasPermission("eco.admin")){
                    player.sendMessage(getLang("commands.no_perm", player));
                    return true;
                 }

                if (args.length < 2) {
                    sender.sendMessage("Usage: /lottery reset <lotery_name>");
                    return true;
                }
                String loteryName4 = args[1];
                // Reset lotery
                plugin.getLoteryManager().resetLotery(loteryName4);
                break;
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lottery delete <lotery_name>");
                    return true;
                }
                if (!player.hasPermission("eco.admin")){
                    player.sendMessage(getLang("commands.no_perm", player));
                    return true;
                 }
                String loteryName5 = args[1];
                // Delete lotery
                plugin.getLoteryManager().deleteLotery(loteryName5);
                break;
            case "create":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /lottery create <lotery_name> <price>");
                    return true;
                }
                if (!player.hasPermission("eco.admin")){
                    player.sendMessage(getLang("commands.no_perm", player));
                    return true;
                 }
                String loteryName1 = args[1];
                int price;
                try {
                    price = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid price.");
                    return true;
                }

                if (plugin.getLoteryManager().getLoteryList().containsKey(loteryName1.toLowerCase())) {
                    sender.sendMessage("Lottery already exists.");
                    return true;
                }

                // Create lotery
                plugin.getLoteryManager().createLotery(loteryName1.toLowerCase(), price);
                break;
            case "draw":
            if (!player.hasPermission("eco.admin")){
                player.sendMessage(getLang("commands.no_perm", player));
                return true;
             }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lottery draw <lotery_name>");
                    return true;
                }
                String loteryName3 = args[1];
                // Draw lotery
                plugin.getLoteryManager().drawLotery(loteryName3);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lottery info <lotery_name>");
                    return true;
                }
                String loteryName2 = args[1];
                // Get lotery info
                plugin.getLoteryManager().getLoteryInfo(player, loteryName2);
                break;
            default:
                sender.sendMessage("Usage: /lottery <buy/info/draw/create/reset/delete>");
                break;
        }
        return true;
    }

}
