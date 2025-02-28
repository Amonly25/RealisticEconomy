package com.ar.askgaming.realisticeconomy.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
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
            List<String> list = new ArrayList<>(List.of("balance","pay","server","top","info")); // Lista modificable
            if (sender.hasPermission("eco.admin")){
                list.add("add");
                list.add("take");
                list.add("set");
                list.add("reset");
                list.add("reload");
            }
            return list;
        }
        return null;
    }

    private String getLang(String key,Player p){
        return plugin.getLang().getFrom(key, p);
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
                   p.sendMessage(getLang("commands.no_perm",p));
                }
                break;
            case "pay":
                payCommand(p, args);
                break;
            case "take":
                if (p.hasPermission("eco.admin")){
                    handleAddOrTake(sender, args, false);
                } else {
                    p.sendMessage(getLang("commands.no_perm",p));
                }
                break;
            case "set":
                if (p.hasPermission("eco.admin")){
                    setCommand(p, args);
                } else {
                    p.sendMessage(getLang("commands.no_perm", p));
                }
                break;
            case "top":
                topCommand(p, args);
                break;
            case "help":
                helpCommand(p,args);    
                break;
            case "info":
                showEcoInfo(p, args);
                break;
            case "reset":
                if (p.hasPermission("eco.admin")){
                    resetCommand(p, args);
                } else {
                    p.sendMessage(getLang("commands.no_perm", p));
                }
                break;
            case "reload":
                if (p.hasPermission("eco.admin")){
                    plugin.reloadConfig();
                    p.sendMessage(getLang("commands.reload", p));
                } else {
                    p.sendMessage(getLang("commands.no_perm", p));
                }
                break;
            default:
                helpCommand(p, args);
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

        int playtime = plugin.getConfig().getInt("playtime_minimum_for_player_pay",6);
        int total_minutes = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
        int hours = total_minutes / 60;
        if (hours < playtime){
            p.sendMessage(getLang("bank.playtime", p).replace("{hours}", playtime+""));
            return;

        }

        OfflinePlayer target = checkPlayer(p, args[1]); 
        if (target == null){
            return;
        }
        double d;
        try {
            d = Double.valueOf(args[2]);
        } catch (Exception e) {
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (pd.isSeizedAccount()) {
            p.sendMessage(getLang("bank.seized_account", p));
            return;
        }
        if (d <= 0){
            p.sendMessage(getLang("error.invalid_amount", p));
            return;
        }
        if (pd.getBalance() < d){
            p.sendMessage(getLang("error.not_enough", p));
            return;
        }
        
        d = round(d);
        boolean transaction = plugin.getEconomyService().playerPayPlayer(p.getUniqueId(), target.getUniqueId(), d);
        if (transaction){
            p.sendMessage(getLang("transactions.pay", p).replace("{player}", args[1]).replace("{amount}", String.valueOf(d)));
            Player player = target.getPlayer();

            if (player != null){
                player.sendMessage(getLang("transactions.pay_notify", player).replace("{player}", p.getName()).replace("{amount}", args[2]));
            }
            plugin.getEconomyLogger().log("Player " + p.getName() + " paid " + args[1] + " " + args[2]);
        } else p.sendMessage(getLang("error.transaction",p));
        
    }
    //#region balance
    public void balanceCommand(Player p, String[] args){
        if (args.length == 2){
            OfflinePlayer player = checkPlayer(p, args[1]);
                
            if (player == null){
                return;
            }
            double balance = plugin.getEconomyService().getBalance(player.getUniqueId());
            
            p.sendMessage(getLang("balance_other", p).replace("{balance}", EconomyManager.format(balance)).replace("{player}", args[1]));
            return;
        }
        PlayerData playerData = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (playerData == null){
            p.sendMessage(getLang("error.player_not_found",p));
            return;
        }
        double balance = playerData.getBalance();
        double bankBalance = playerData.getBankBalance();
       
        p.sendMessage(getLang("balance", p).replace("{balance}", balance+""));
        p.sendMessage(getLang("bank_balance", p).replace("{balance}", bankBalance+""));

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
        OfflinePlayer target = checkPlayer(sender, args[1]);
    
        boolean transactionSuccess;
        if (isAdd) {
            transactionSuccess = plugin.getServerBank().withdrawFromServerToPlayer(target.getUniqueId(), amount);
        } else {
            transactionSuccess = plugin.getServerBank().depositFromPlayerToServer(target.getUniqueId(), amount);
        }
    
        if (transactionSuccess) {
            String action = isAdd ? "added" : "took";
            String message = sender.getName() + " " + action + " " + args[2] + " " + (isAdd ? "to" : "from") + " " + args[1];
            sender.sendMessage(message);

            plugin.getEconomyLogger().log(message);
            Player player = target.getPlayer();
            if (player != null) {
                player.sendMessage(getLang("economy." + (isAdd ? "add" : "take") + "_player_notify", player).replace("{amount}", args[2]));
            }
        } else {
            sender.sendMessage("§cError, transaction failed.");
        }
    }
    public void handleServerCommand(Player p, String[] args){
        double d = plugin.getServerBank().getBalance();
        p.sendMessage(getLang("server_balance", p).replace("{balance}", EconomyManager.format(d)));
    }
    //#region set
    private List<Player> warn = new ArrayList<>();

    public void setCommand(Player p, String[] args){

        if (!preCommand(p, args)){
            return;
        }
        double d = round(Double.valueOf(args[2]));
        OfflinePlayer target = checkPlayer(p, args[1]);

        if (!warn.contains(p)){
            p.sendMessage("§cWarning: This command will set the balance of the player without bank transaction.");
            p.sendMessage("§cThis action can disturb the economy of the server, use with caution.");
            p.sendMessage("§cIf you are sure, use the command again.");
            warn.add(p);
            return;
        }
        warn.remove(p);

        boolean transaction = plugin.getEconomyService().setPlayerBalance(target.getUniqueId(), d);
        if (transaction){
            p.sendMessage(getLang("economy.set_player", p).replace("{player}", args[1]).replace("{amount}", args[2]));
            Player player = target.getPlayer();
            if (player != null){
                player.sendMessage(getLang("economy.set_player_notify", player).replace("{amount}", args[2]));
            }
            plugin.getEconomyLogger().log("Player " + p.getName() + " set " + args[1] + " to " + args[2]);
        } else p.sendMessage(getLang("error.transaction",p));
        
    }
    //#region reset
    public void resetCommand(Player p, String[] args){

        OfflinePlayer player = checkPlayer(p, args[1]);
        if (player == null){
            return;
        }

        if (!warn.contains(p)){
            p.sendMessage("§cWarning: This command will set the balance of the player without bank transaction.");
            p.sendMessage("§cThis action can disturb the economy of the server, use with caution.");
            p.sendMessage("§cIf you are sure, use the command again.");
            warn.add(p);
            return;
        }
        warn.remove(p);

        PlayerData pd = plugin.getDatabase().loadPlayerData(player.getUniqueId());
        pd.setBalance(0);
        pd.setBankBalance(0);
        pd.setDebt(0);
        pd.setSeized_account(false);
        pd.save();
        p.sendMessage("§aPlayer " + args[1] + " has been reset.");
        
    }
    //#region top
    public void topCommand(Player p, String[] args){
        
        List <String> list = new ArrayList<>();

        HashMap<String, Double> balances = plugin.getDatabase().getBalances();
        balances.entrySet().stream().sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(e.getKey()));
            list.add(player.getName() + " - " + EconomyManager.format(e.getValue()));
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
        sender.sendMessage(getLang("error.player_not_found",null));
        return null;
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
    //#region info
    private void showEcoInfo(Player p, String[] args){ 
        double d = plugin.getServerBank().getBalance();
        p.sendMessage(getLang("server_balance", p).replace("{balance}", EconomyManager.format(d)));

        double players = plugin.getEconomyManager().getPlayerBalances();
        p.sendMessage(getLang("bank.info.players", p).replace("{value}", EconomyManager.format(players)));

        plugin.getEconomyManager().calculateInflation();
        String inf = EconomyManager.format(plugin.getEconomyManager().getInflation());
        p.sendMessage(getLang("bank.info.inflation", p).replace("{value}", inf));
        

    }
}
