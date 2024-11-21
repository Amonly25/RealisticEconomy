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
import com.ar.askgaming.realisticeconomy.Data.LangManager;

public class LoteryManager {
    
    private RealisticEconomy plugin;
    private File loteryFile;
    private FileConfiguration loteryConfig;
    private boolean maintenance = false;

    public LoteryManager(RealisticEconomy main) {
        plugin = main;

        loteryFile = new File(plugin.getDataFolder(), "lotery.yml");
        if (!loteryFile.exists()) {
            plugin.saveResource("lotery.yml", false);
        }

        loteryConfig = new YamlConfiguration();
        try {
            loteryConfig.load(loteryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Obtener todas las claves del nivel raíz
        Set<String> keys = loteryConfig.getKeys(false);

        // Iterar sobre todas las keys y cargar cada loteria
        for (String key : keys) {
            Object obj = loteryConfig.get(key);
            if (obj instanceof Lotery) {
                Lotery protection = (Lotery) obj;

                loteryList.put(key, protection);
            }
        }
    }
    private LangManager lang = plugin.getLang();

    private HashMap<String, Lotery> loteryList = new HashMap<>();

    public void createLotery(String name, int ticketPrice, int numberLimit) {
        Lotery lotery = new Lotery(name, ticketPrice, numberLimit);

        loteryList.put(name, lotery);

        lang.broadcastTranslated("lottery.created","{name}", name);
          
    }
    public void drawLotery(String name) {
        Lotery lotery = loteryList.get(name);
        if (lotery == null || !lotery.isActive()) return;

        lotery.setActive(false);
        int winningNumber = (int) (Math.random() * lotery.getNumberLimit()) + 1;
        lotery.setWinningNumber(winningNumber);

        List<UUID> winners = getWinners(lotery);
        boolean hasWinners = !winners.isEmpty();
        double prize = hasWinners ? lotery.getJackpot() / winners.size() : 0;
        lotery.setJackpot(0);
        saveConfig();
        lang.broadcastTranslated("lottery.drawn","{name}", name);
        lang.broadcastTranslated("lottery.number","{number}", String.valueOf(winningNumber));
       
        if (hasWinners) {
            processWinners(name, prize, winners);
        } else {
            lang.broadcastTranslated("lottery.no_winners","{none}", "");
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
    // Process the winners and distribute the prize
    private void processWinners(String name, double prize, List<UUID> winners) {
        for (UUID winner : winners) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(winner);
            boolean deposit = plugin.getEconomyService().depositPlayer(winner, prize);
            double taxes = prize * 0.1;
            boolean taken = plugin.getServerBank().depositFromPlayerToServer(winner, taxes);

            //Verificar la transacción

            if (player.isOnline()) {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer != null) {
                    onlinePlayer.sendMessage(lang.getFrom("lottery.won", String.valueOf(prize)));
                    onlinePlayer.sendMessage(lang.getFrom("lottery.taxes", String.valueOf(taxes)));

                }
            }
        }
        lang.broadcastTranslated("lottery.winners","{list}", winners.toString());
        lang.broadcastTranslated("lottery.reward","{amount}", String.valueOf(prize));

    }
    // Buy a ticket for the lottery
    public void buyTicket(Player p, String name, int number) {
        Lotery lotery = loteryList.get(name);
        if (lotery == null || !lotery.isActive()) {
            p.sendMessage(lang.getFrom("lottery.no_active", p.getLocale()));
            return;
        }
        if (number < 1 || number > lotery.getNumberLimit()) {
            p.sendMessage(lang.getFrom("lottery.number_limit", p.getLocale()).replace("{number}", String.valueOf(lotery.getNumberLimit())));
            return;
        }
        if (plugin.getEconomyService().getBalance(p.getUniqueId()) < lotery.getTicketPrice()) {
            p.sendMessage(lang.getFrom("error.not_enough", p.getLocale()));
            return;
        }
        if (plugin.getEconomyService().withdrawPlayer(p.getUniqueId(), lotery.getTicketPrice())) {
            lotery.addTicket(p.getUniqueId(), number);
            p.sendMessage(lang.getFrom("lottery.buy", p.getLocale()).replace("{number}", String.valueOf(number)));
            lang.broadcastTranslated("lottery.player_buy", "{name}", name);
        } else {
            p.sendMessage(lang.getFrom("error.transaction", p.getLocale()));
        }
    }
    // Get information about a specific lottery
    public void getLoteryInfo(Player p, String name) {
        Lotery lotery = loteryList.get(name);
        if (lotery == null) return;

        p.sendMessage(lang.getFrom("lottery.info.name", p.getLocale()).replace("{name}", name));
        p.sendMessage(lang.getFrom("lottery.info.ticket_price", p.getLocale()).replace("{price}", String.valueOf(lotery.getTicketPrice())));
        p.sendMessage(lang.getFrom("lottery.info.number_limit", p.getLocale()).replace("{limit}", String.valueOf(lotery.getNumberLimit())));
        p.sendMessage(lang.getFrom("lottery.info.tickets_sold", p.getLocale()).replace("{sold}", String.valueOf(lotery.getTicketsSold())));
        p.sendMessage(lang.getFrom("lottery.info.jackpot", p.getLocale()).replace("{jackpot}", String.valueOf(lotery.getJackpot())));
        p.sendMessage(lang.getFrom("lottery.info.active", p.getLocale()).replace("{active}", String.valueOf(lotery.isActive())));

    }
    // Delete a lottery
    public void deleteLotery(String name) {
        if (loteryList.containsKey(name)) {
            loteryList.remove(name);
            loteryConfig.set(name, null);
            saveConfig();
            lang.broadcastTranslated("lottery.remove", "{name}", name);
        }
    }

    // Reset a lottery
    public void resetLotery(String name) {
        Lotery lotery = loteryList.get(name);
        if (lotery != null) {
            lotery.reset();
            lang.broadcastTranslated("lottery.reset", "{name}", name);
            saveConfig();
        }
    }

    // Save the lottery configuration file
    private void saveConfig() {
        try {
            loteryConfig.save(loteryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isMaintenance() {
        return maintenance;
    }
    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}
