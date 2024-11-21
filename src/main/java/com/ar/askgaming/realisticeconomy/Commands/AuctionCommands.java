package com.ar.askgaming.realisticeconomy.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class AuctionCommands implements TabExecutor{

    private RealisticEconomy plugin;
    public AuctionCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("info", "create", "bet", "set_item");
        } else return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /auction <info/create/bet/set_item>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;

        if (plugin.getAuctionManager().isMaintenance()) {
            p.sendMessage("§cAuction is in maintenance mode.");
            return true;

        }

        switch (args[0].toLowerCase()) {
            case "create":
                
                break;
            case "info":
                    
                break;
            case "bet":
                    
                break;
            case "set_item":
                        
                break;

            default:
                p.sendMessage("Usage: /auction <info/create/bet/set_item>");
                break;
        }

        return true;
    }

}
