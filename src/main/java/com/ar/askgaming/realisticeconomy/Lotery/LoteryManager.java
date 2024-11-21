package com.ar.askgaming.realisticeconomy.Lotery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class LoteryManager {
    
    private RealisticEconomy plugin;
    private File LoteryFile;
    private FileConfiguration LoteryConfig;
    public LoteryManager(RealisticEconomy main) {
        plugin = main;

        LoteryFile = new File(plugin.getDataFolder(), "lotery.yml");
        if (!LoteryFile.exists()) {
            plugin.saveResource("lotery.yml", false);
        }

        LoteryConfig = new YamlConfiguration();
        try {
            LoteryConfig.load(LoteryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Obtener todas las claves del nivel raíz
        Set<String> keys = LoteryConfig.getKeys(false);

        // Iterar sobre todas las keys y cargar cada Protection
        for (String key : keys) {
            Object obj = LoteryConfig.get(key);
            if (obj instanceof Lotery) {
                Lotery protection = (Lotery) obj;

                // Guardar cada Protection en el mapa con su clave
                loteryList.put(key, protection);
            }
        }
    }

    private HashMap<String, Lotery> loteryList = new HashMap<>();

    public void createLotery(String name, int ticketPrice, int numberLimit) {
        Lotery lotery = new Lotery(name, ticketPrice, numberLimit);

        loteryList.put(name, lotery);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage("A new lotery has been created: " + name);

        }
        
    }
    public void drawLotery(String name) {
        Lotery lotery = null;

        if (loteryList.containsKey(name)) {
            lotery = loteryList.get(name);
        } else return;

        if (lotery.isActive()) {
            lotery.setActive(false);

            int winningNumber = (int) (Math.random() * lotery.getNumberLimit()) + 1;
            lotery.setWinningNumber(winningNumber);

            List<UUID> winners = getWinners(lotery);
            boolean hasWinners = winners.size() > 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("The lotery has been drawn: " + name);
                p.sendMessage("The winning number is: " + winningNumber);

            }
            if (hasWinners){
                double prize = lotery.getJackpot() / winners.size();
                List<String> winnersNames = new ArrayList<>();
                for (UUID winner : winners) {

                    OfflinePlayer player = Bukkit.getOfflinePlayer(winner);
                    plugin.getEconomyService().depositPlayer(winner, prize);
                    winnersNames.add(player.getName());
                    
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    
                    p.sendMessage("The winners are: " + winners.toString() + ", hey have been awarded: " + prize);
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("There are no winners this time.");
                    p.sendMessage("The jackpot will be remain to the next lotery: " + lotery.getJackpot());
                }
            }
        }
    }
    private List<UUID> getWinners(Lotery lotery){
        List<UUID> winners = new ArrayList<>();

        int winningNumber = lotery.getWinningNumber();

        for (UUID player : lotery.getTickets().keySet()) {
            int ticketNumber = lotery.getTickets().get(player);

            if (ticketNumber == winningNumber) {
                winners.add(player);
            }
        }

        return winners;
    }
    public void buyTicket(Player p, String name, int number) {
        Lotery lotery = null;

        if (loteryList.containsKey(name)) {
            lotery = loteryList.get(name);
        } else return;

        if (lotery.isActive()) {

            if (number > lotery.getNumberLimit() || number < 1) {
                p.sendMessage("Invalid number, the number must be between 1 and " + lotery.getNumberLimit());
                return;
            }
            if (plugin.getEconomyService().getBalance(p.getUniqueId()) < lotery.getTicketPrice()) {
                p.sendMessage("You don't have enough money to buy a ticket.");
                return;
            }
            if (plugin.getEconomyService().withdrawPlayer(p.getUniqueId(), lotery.getTicketPrice())){

                lotery.addTicket(p.getUniqueId(), number);
                p.sendMessage("You have bought a ticket for the lotery: " + name);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(p.getName() + " has bought a ticket for the lotery: " + name);
                }
            } else {
                p.sendMessage("An error occurred while buying the ticket, you have money?");
            }

        } else {
            p.sendMessage("The lotery is not active.");
        }
    }
    public void getLoteryInfo(Player p, String name) {
        Lotery lotery = null;

        if (loteryList.containsKey(name)) {
            lotery = loteryList.get(name);
        } else return;

        p.sendMessage("Lotery: " + name);
        p.sendMessage("Ticket price: " + lotery.getTicketPrice());
        p.sendMessage("Number limit: " + lotery.getNumberLimit());
        p.sendMessage("Tickets sold: " + lotery.getTicketsSold());
        p.sendMessage("Jackpot: " + lotery.getJackpot());
        p.sendMessage("Active: " + lotery.isActive());
    }
}
