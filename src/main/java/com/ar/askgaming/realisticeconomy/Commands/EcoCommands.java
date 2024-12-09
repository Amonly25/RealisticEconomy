package com.ar.askgaming.realisticeconomy.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;
import com.ar.askgaming.realisticeconomy.Economy.EconomyManager;

public class EcoCommands implements TabExecutor{

    private final RealisticEconomy plugin;
    public EcoCommands(RealisticEconomy main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            return List.of("balance","add","take","pay","server","top");

        } else return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)){
            switch (args[0].toLowerCase()) {
                case "add":
                    handleAddOrTake(sender, args, true);
                    break;
                case "take":
                    handleAddOrTake(sender, args, false);
                    break;
                default:
                    sender.sendMessage("Error, only /eco add and /eco take can be executed by console.");
                    break;
            }
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0){
            balanceCommand(p, args);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance":
                balanceCommand(p, args);
                break;
            case "server":
                handleServerCommand(p, args);
                break;
            case "add":
                if (p.hasPermission("eco.admin")){
                    handleAddOrTake(sender, args, true);
                } else {
                   p.sendMessage(plugin.getLang().getFrom("commands.no_perm",p.getLocale()));
                }
                break;
            case "pay":
                payCommand(p, args);
                break;
            case "take":
                if (p.hasPermission("eco.admin")){
                    handleAddOrTake(sender, args, false);
                } else {
                    p.sendMessage(plugin.getLang().getFrom("commands.no_perm",p.getLocale()));
                }
                break;
            case "set":
                if (p.hasPermission("eco.admin")){
                    setCommand(p, args);
                } else {
                    p.sendMessage(plugin.getLang().getFrom("commands.no_perm", p.getLocale()));
                }
                break;
            case "top":
                topCommand(p, args);
                break;
            case "help":
                helpCommand(p,args);    
                break;
            default:
                p.sendMessage("Error, use: /eco <balance|server|add|pay|take>");
                break;
        }
        return true;
    }

    private double round(double d){
        return EconomyManager.formatDouble(d);
    }
    //#region pay
    private void payCommand(Player p, String[] args) {

        if (args.length != 3){
            p.sendMessage("Error, use: /eco pay <player> <amount>");
            return;
        }

        OfflinePlayer player = checkPlayer(p, args[1]); 
        if (player == null){
            return;
        }
        double d;
        try {
            d = Double.valueOf(args[2]);
        } catch (Exception e) {
            p.sendMessage(getMsg("error.invalid_amount", p));
            return;
        }
        d = round(d);
        boolean transaction = plugin.getEconomyService().playerPayPlayer(p.getUniqueId(), player.getUniqueId(), d);
        if (transaction){
            p.sendMessage(plugin.getLang().getFrom("transactions.pay", p.getLocale()).replace("{player}", args[1]).replace("{amount}", String.valueOf(d)));
            if (player.isOnline()){
                player.getPlayer().sendMessage(plugin.getLang().getFrom("transactions.pay_notify", player.getPlayer().getLocale()).replace("{player}", p.getName()).replace("{amount}", args[2]));
            }
            plugin.getEconomyLogger().log("Player " + p.getName() + " paid " + args[1] + " " + args[2]);
        } else p.sendMessage(getMsg("error.transaction",p));
        
    }
    //#region balance
    public void balanceCommand(Player p, String[] args){
        if (args.length == 2){
            OfflinePlayer player = checkPlayer(p, args[1]);
                
            if (player == null){
                return;
            }
            double balance = plugin.getEconomyService().getBalance(player.getUniqueId());
            
            p.sendMessage(plugin.getLang().getFrom("balance_other", p.getLocale()).replace("{balance}", balance+"").replace("{player}", args[1]));
            return;
        }
        PlayerData playerData = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (playerData == null){
            p.sendMessage(plugin.getLang().getFrom("error.player_not_found",p.getLocale()));
            return;
        }
        double balance = playerData.getBalance();
        double bankBalance = playerData.getBankBalance();
       
        p.sendMessage(plugin.getLang().getFrom("balance", p.getLocale()).replace("{balance}", balance+""));
        p.sendMessage(plugin.getLang().getFrom("bank_balance", p.getLocale()).replace("{balance}", bankBalance+""));

    }
    //#region add/take
    private boolean preCommand(CommandSender sender, String[] args){
        if (args.length != 3){
            sender.sendMessage("Error, use: /eco add/take <player> <amount>");
            return false;
        }
        OfflinePlayer player = checkPlayer(sender, args[1]);
            
        if (player == null){
            return false;
        }
        try {
            Double.valueOf(args[2]);    
        } catch (Exception e) {
            sender.sendMessage("Error, invalid amount.");
            return false;
        } 
        
        return true;

    }
    private void handleAddOrTake(CommandSender sender, String[] args, boolean isAdd) {
        if (!preCommand(sender, args)) {
            return;
        }
    
        double amount = round(Double.valueOf(args[2]));
        OfflinePlayer player = checkPlayer(sender, args[1]);
    
        boolean transactionSuccess;
        if (isAdd) {
            transactionSuccess = plugin.getServerBank().withdrawFromServerToPlayer(player.getUniqueId(), amount);
        } else {
            transactionSuccess = plugin.getServerBank().depositFromPlayerToServer(player.getUniqueId(), amount);
        }
    
        if (transactionSuccess) {
            String action = isAdd ? "added" : "took";
            String message = sender.getName() + " " + action + " " + args[2] + " " + (isAdd ? "to" : "from") + " " + args[1];
            sender.sendMessage(message);
            plugin.getEconomyLogger().log(message);
            if (player.isOnline()) {
                player.getPlayer().sendMessage(plugin.getLang().getFrom("economy." + (isAdd ? "add" : "take") + "_player_notify", player.getPlayer().getLocale()).replace("{amount}", args[2]));
            }
        } else {
            sender.sendMessage("§cError, transaction failed.");
        }
    }
    public void handleServerCommand(Player p, String[] args){
        double d = plugin.getServerBank().getBalance();
        p.sendMessage(plugin.getLang().getFrom("server_balance", p.getLocale()).replace("{balance}", String.valueOf(d)));
    }
    //#region set
    private List<Player> warn = new ArrayList<>();

    public void setCommand(Player p, String[] args){

        if (!preCommand(p, args)){
            return;
        }
        double d = round(Double.valueOf(args[2]));
        OfflinePlayer player = checkPlayer(p, args[1]);

        if (!warn.contains(p)){
            p.sendMessage("§cWarning: This command will set the balance of the player without bank transaction.");
            p.sendMessage("§cThis action can disturb the economy of the server, use with caution.");
            p.sendMessage("§cIf you are sure, use the command again.");
            warn.add(p);
            return;
        }
        warn.remove(p);

        boolean transaction = plugin.getEconomyService().setPlayerBalance(player.getUniqueId(), d);
        if (transaction){
            p.sendMessage(plugin.getLang().getFrom("economy.set_player", p.getLocale()).replace("{player}", args[1]).replace("{amount}", args[2]));
            if (player.isOnline()){
                player.getPlayer().sendMessage(plugin.getLang().getFrom("economy.set_player_notify", player.getPlayer().getLocale()).replace("{amount}", args[2]));
            }
            plugin.getEconomyLogger().log("Player " + p.getName() + " set " + args[1] + " to " + args[2]);
        } else p.sendMessage(getMsg("error.transaction",p));
        
    }
    //#region top
    public void topCommand(Player p, String[] args){
        
        List <String> list = new ArrayList<>();

        HashMap<String, Double> balances = plugin.getDatabase().getBalances();
        balances.entrySet().stream().sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(e.getKey()));
            list.add(player.getName() + " - " + e.getValue());
        });

        plugin.getUtilityMethods().listToPage(list, args, p);
    }

    private OfflinePlayer checkPlayer(CommandSender sender,String name){
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player.isOnline()){
            return player;
        }
        if (player.hasPlayedBefore()){
            return player;
        }
        sender.sendMessage(plugin.getLang().getFrom("error.player_not_found","EN_US"));
        return null;
    }
    private String getMsg(String key,Player p){
        return plugin.getLang().getFrom(key, p.getLocale());
    }

    //#region help
    private void helpCommand(Player p, String[] args){
        p.sendMessage("§6/eco balance [player]");
        p.sendMessage("§6/eco add <player> <amount>");
        p.sendMessage("§6/eco take <player> <amount>");
        p.sendMessage("§6/eco pay <player> <amount>");
        p.sendMessage("§6/eco server");
        p.sendMessage("§6/eco top");
    }
}
