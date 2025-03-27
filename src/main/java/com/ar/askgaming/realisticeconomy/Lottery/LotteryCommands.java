package com.ar.askgaming.realisticeconomy.Lottery;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class LotteryCommands implements TabExecutor {

    private final RealisticEconomy plugin;
    public LotteryCommands() {
        plugin = RealisticEconomy.getInstance();

        plugin.getServer().getPluginCommand("lottery").setExecutor(this);
    }

    private List<String> adminCommands = new ArrayList<>(List.of("draw", "create", "reset", "delete"));

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(List.of("info", "buy"));
            if (sender.hasPermission("eco.admin")) {
                list.add("draw");
                list.add("create");
                list.add("reset");
                list.add("delete");
            }
            return list;
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
           sender.sendMessage("Please specify a subcommand.");
           return true;
       }
       if (!(sender instanceof Player)) {
           sender.sendMessage("Only players can use this command.");
           return true;
       }
       Player player = (Player) sender;

       if (adminCommands.contains(args[0].toLowerCase()) && !player.hasPermission("eco.admin")) {
           player.sendMessage(getLang("commands.no_perm", player));
           return true;
       }

        switch (args[0].toLowerCase()) {
            case "buy":
                buy(player, args);
                break;
            case "reset":
                reset(player, args);
                break;
            case "delete":
                delete(player, args);
                break;
            case "create":
                create(player, args);
                break;
            case "draw":
                draw(player, args);
                break;
            case "info":
                info(player, args);
                break;
            default:
                sender.sendMessage("Invalid subcommand.");
                break;
        }
        return true;
    }
    //#region create
    private void create(Player player, String[] args){
        if (args.length < 3) {
            player.sendMessage("Usage: /lottery create <lotery_name> <price>");
            return;
        }

        String loteryName1 = args[1];
        int price;
        try {
            price = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number.");
            return;
        }

        if (plugin.getLoteryManager().getLoteryList().containsKey(loteryName1.toLowerCase())) {
            player.sendMessage("Lottery already exists.");
            return;
        }

        plugin.getLoteryManager().createLotery(loteryName1.toLowerCase(), price);
    }
    //#region buy
    private void buy(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /lottery buy <lotery_name> <number>");
            return;
        }
        String loteryName = args[1];
        int number;
        try {
            number = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number.");
            return;
        }

        plugin.getLoteryManager().buyTicket(player, loteryName, number);
    }
    //#region draw
    private void draw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /lottery draw <lotery_name>");
            return;
        }
        String loteryName3 = args[1];
        plugin.getLoteryManager().drawLotery(loteryName3);
    }
 
    //#region reset
    private void reset(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /lottery reset <lotery_name>");
            return;
        }
        String loteryName4 = args[1];
        plugin.getLoteryManager().resetLotery(loteryName4);
    }
    //#region delete
    private void delete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /lottery delete <lotery_name>");
            return;
        }
        String loteryName5 = args[1];
        plugin.getLoteryManager().deleteLotery(loteryName5);
    }
    //#region info
    private void info(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /lottery info <lotery_name>");
            return;
        }
        String loteryName2 = args[1];
        plugin.getLoteryManager().getLoteryInfo(player, loteryName2);
    }

}
