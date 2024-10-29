package com.ar.askgaming.realisticeconomy;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class Commands implements TabExecutor{

    private final Main plugin;
    public Commands(Main main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        return List.of("balance");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        switch (args[0].toLowerCase()) {
            case "balance":
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        sender.sendMessage("Tu balance es: " + plugin.getEconomyService().getBalance(player.getName()));
                    }
                break;
            case "add":
                if (args.length == 2){
                    if (plugin.getEconomyService().hasAccount(args[1])){
                        plugin.getEconomyService().depositPlayer(args[1], 100);
                        sender.sendMessage("Has añadido 100 al balance de " + args[1]);
                    }
                }
                break;
            case "take":
                if (args.length == 2){
                    if (plugin.getEconomyService().hasAccount(args[1])){
                        plugin.getEconomyService().withdrawPlayer(args[1], 20);
                        sender.sendMessage("Has quitado 20 al balance de " + args[1]);
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

}
