package com.ar.askgaming.realisticeconomy.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class LoteryCommands implements TabExecutor {

    private RealisticEconomy plugin;
    public LoteryCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("buy", "info", "draw", "create", "reset", "delete");
        } else return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       if (args.length == 0) {
           sender.sendMessage("Usage: /lotery <buy/info/draw>");
           return true;
       }
       if (!(sender instanceof Player)) {
           sender.sendMessage("Only players can use this command.");
           return true;
       }
       Player player = (Player) sender;

       if (plugin.getLoteryManager().isMaintenance()) {
           player.sendMessage("§cLotery is in maintenance mode.");
           return true;
       }


       switch (args[0].toLowerCase()) {
            case "buy":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /lotery buy <lotery_name> <number>");
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
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lotery reset <lotery_name>");
                    return true;
                }
                String loteryName4 = args[1];
                // Reset lotery
                plugin.getLoteryManager().resetLotery(loteryName4);
                break;
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lotery delete <lotery_name>");
                    return true;
                }
                String loteryName5 = args[1];
                // Delete lotery
                plugin.getLoteryManager().deleteLotery(loteryName5);
                break;
            case "create":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /lotery create <lotery_name> <price>");
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
                // Create lotery
                plugin.getLoteryManager().createLotery(loteryName1, price, 50);
                break;
            case "draw":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lotery draw <lotery_name>");
                    return true;
                }
                String loteryName3 = args[1];
                // Draw lotery
                plugin.getLoteryManager().drawLotery(loteryName3);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /lotery info <lotery_name>");
                    return true;
                }
                String loteryName2 = args[1];
                // Get lotery info
                plugin.getLoteryManager().getLoteryInfo(player, loteryName2);
                break;
            default:
                sender.sendMessage("Usage: /lotery <buy/info>");
                break;
        }
        return true;
    }

}
