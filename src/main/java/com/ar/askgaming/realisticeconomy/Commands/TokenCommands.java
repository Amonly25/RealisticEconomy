package com.ar.askgaming.realisticeconomy.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            List<String> list = new ArrayList<>(List.of("shop", "balance")); // Lista modificable
            if (sender.hasPermission("eco.admin")) {
                list.add("reload");
                list.add("additem");
                list.add("give");
            }
            if (sender.hasPermission("eco.tokens.pay")) {
                list.add("pay");
            }
            return list;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /token <help>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "balance":
                balance(sender,args);
                break;
            case "pay":
                pay(sender, args);
                break;
            case "shop":
                openShop(sender);
                break;
            case "additem":
                addShopItem(sender);
                break;
            case "give":
                give(sender, args);
                break;
            case "reload":
                plugin.getTokenShop().loadConfig();
                sender.sendMessage("§aConfig reloaded");
                break;
            default:
                sender.sendMessage("§cUsage: /token <pay/shop/balance>");
                break;
        }
        return true;
    }
    //#region balance
    private void balance(CommandSender sender, String[] args){
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return;
        }
        Player p = (Player) sender;
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());
        if (pd == null) {
            p.sendMessage("§cPlayer data not found.");
            return;
        }
        p.sendMessage(plugin.getLang().getFrom("tokens", p.getLocale()).replace("{amount}", String.valueOf(pd.getTokens())));
    }
    //#region pay
    private void pay(CommandSender sender, String[] args){
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return;
        }
        Player p = (Player) sender;
        if (args.length < 3) {
            p.sendMessage("§cUsage: /token pay <player> <amount>");
            return;
        }
        if (!p.hasPermission("eco.tokens.pay")){
            p.sendMessage(plugin.getLang().getFrom("error.no_permission",p.getLocale()));
            return;
        }
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
    //#region shop
    private void addShopItem(CommandSender sender){
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("eco.admin")){
            p.sendMessage(plugin.getLang().getFrom("error.no_permission",p.getLocale()));
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()){
            p.sendMessage("§cYou must hold an item in your hand");
            return;
        }
        plugin.getTokenShop().createNew(item);
        p.sendMessage("§aItem added to the shop");
        p.sendMessage("§aConfig the item in the tokenshop.yml file, then use /token reload to apply the changes");
    }
    //#region shop
    private void openShop(CommandSender sender){
        if (!(sender instanceof Player)){
            sender.sendMessage("§cYou must be a player to use this command");
            return;
        }
        Player p = (Player) sender;
        plugin.getTokenShop().openInventory(p, 0);
        return;

    }
    //#region give
    private void give(CommandSender sender, String[] args){
        if (!sender.hasPermission("eco.admin")){
            sender.sendMessage(plugin.getLang().getFrom("error.no_permission","en"));
            return;
        }
        if (args.length < 3){
            sender.sendMessage("§cUsage: /token give <player> <amount>");
            return;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);

        PlayerData pd = plugin.getDatabase().loadPlayerData(target.getUniqueId());
        if (pd == null){
            sender.sendMessage(plugin.getLang().getFrom("error.player_not_found","en"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (Exception e) {
            sender.sendMessage(plugin.getLang().getFrom("error.invalid_amount","en"));
            return;
        }
        if (amount <= 0){
            sender.sendMessage(plugin.getLang().getFrom("error.invalid_amount","en"));
            return;
        }
        pd.setTokens(pd.getTokens() + amount);
        pd.save();
        sender.sendMessage(plugin.getLang().getFrom("token.give","en").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)));
        Player p = target.getPlayer();
        if (p != null){
            p.sendMessage(plugin.getLang().getFrom("token.receive",p.getLocale()).replace("{amount}", String.valueOf(amount)));
        }
    }
}
