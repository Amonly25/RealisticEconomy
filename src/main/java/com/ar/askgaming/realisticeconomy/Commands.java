package com.ar.askgaming.realisticeconomy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;

public class Commands implements TabExecutor{

    private final Main plugin;
    public Commands(Main main) {
        plugin = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        return List.of("balance","add","take");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        switch (args[0].toLowerCase()) {
            case "balance":
                    handleBalanceCommand(sender, args);
                break;
            case "setup":
                handleSetupCommand(sender, args);
            break;
            case "add":
                handleAddCommand(sender, args);
                break;
            case "take":
                handleTakeCommand(sender, args);
                break;
            case "check":
                if (args.length == 2){
                    Player p = plugin.getServer().getPlayer(args[1]);
                    if (p != null){
                        sender.sendMessage(p.getUniqueId().toString());
                    } else {
                        @SuppressWarnings("deprecation")
                        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(args[1]);
                        sender.sendMessage(offPlayer.getUniqueId().toString());
                    }
                }
                
                break;
            default:
                break;
        }
        return false;
    }
    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            try {
                double d = Double.valueOf(args[1]);
                if (sender instanceof ConsoleCommandSender){
                    // revisar si ya ha sido configurado
                    plugin.getServerBank().setup(d);
                    sender.sendMessage("ServerBank configurado.");
                } else {
                    sender.sendMessage("Este comando solo puede ser ejecutado por la consola.");
                }
            } catch (Exception e) {
                sender.sendMessage("No es un número válido.");
            }
        } else {
            sender.sendMessage("Faltan argumentos, uso: /eco setup <cantidad>");
        }
    }

    public void handleBalanceCommand(CommandSender sender, String[] args){
        if (args.length == 1){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.sendMessage("Tu balance es: " + plugin.getEconomyService().getBalance(p));
            } else {
                sender.sendMessage("El balance del banco es: "+ plugin.getServerBank().getBalance());
            }
        } else if (args.length == 2){
            double balance = plugin.getEconomyService().getBalance(args[1]);
            sender.sendMessage("El balance de " + args[1] + " es: " + balance);

        }
    }
    public void handleAddCommand(CommandSender sender, String[] args){
        if (args.length == 3){
            try {
                EconomyResponse e = plugin.getEconomyService().depositPlayer(args[1], Double.valueOf(args[2]));
                plugin.getLogger().info(e.errorMessage);
    
                if (e.type == EconomyResponse.ResponseType.SUCCESS){
                    sender.sendMessage("Has añadido "+ e.amount + " al balance de " + args[1]);
                } else {
                    sender.sendMessage("Error al añadir dinero al balance de " + args[1]);
                }  
            } catch (Exception e) {
                sender.sendMessage("No es un número válido.");
            } 
        } else {
            sender.sendMessage("Faltan argumentos, uso: /eco add <jugador> <cantidad>");
        }
    }
    public void handleTakeCommand(CommandSender sender, String[] args){
        if (args.length == 3){
            try {
                EconomyResponse e = plugin.getEconomyService().withdrawPlayer(args[1], Double.valueOf(args[2]));
                plugin.getLogger().info(e.errorMessage);
                if (e.type == EconomyResponse.ResponseType.SUCCESS){
                    sender.sendMessage("Has quitado "+e.amount+" al balance de " + args[1]);
                } else {
                    sender.sendMessage("Error al quitar dinero al balance de " + args[1]);
                }
            } catch (Exception e) {
                sender.sendMessage("No es un número válido.");
            }
        } else {
            sender.sendMessage("Faltan argumentos, uso: /eco take <jugador> <cantidad>");
        }
    }
}
