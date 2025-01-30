package com.ar.askgaming.realisticeconomy.Lottery;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.LangManager;

public class LotteryManager extends BukkitRunnable {
    
    private RealisticEconomy plugin;
    private File loteryFile;
    private FileConfiguration loteryConfig;

    public LotteryManager(RealisticEconomy main) {
        plugin = main;
        lang = plugin.getLang();

        loteryFile = new File(plugin.getDataFolder(), "lottery.yml");
        if (!loteryFile.exists()) {
            plugin.saveResource("lottery.yml", false);
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
            if (obj instanceof Lottery) {
                Lottery protection = (Lottery) obj;

                loteryList.put(key, protection);
            }
        }
        runTaskTimer(plugin, 0, 20*60);
    }
    private LangManager lang;

    private HashMap<String, Lottery> loteryList = new HashMap<>();

    public HashMap<String, Lottery> getLoteryList() {
        return loteryList;
    }
    public void createLotery(String name, int ticketPrice) {

        int maxNumbers = plugin.getConfig().getInt("lottery.max_numbers",50);
        double jackpot = plugin.getConfig().getDouble("lottery.jackpot",100);

        Lottery lotery = new Lottery(name, ticketPrice, maxNumbers, jackpot);

        loteryList.put(name, lotery);
        loteryConfig.set(name, lotery);
        saveConfig();

        lang.broadcastTranslated("lottery.created","{name}", name);
          
    }
    public void drawLotery(String name) {
        Lottery lotery = loteryList.get(name);
        if (lotery == null || !lotery.isActive()) return;

        lotery.setActive(false);
        int winningNumber = (int) (Math.random() * lotery.getNumberLimit()) + 1;
        lotery.setWinningNumber(winningNumber);

        List<UUID> winners = getWinners(lotery);
        boolean hasWinners = !winners.isEmpty();
        double prize = hasWinners ? lotery.getJackpot() / winners.size() : 0;
        lang.broadcastTranslated("lottery.drawn","{name}", name);
        lang.broadcastTranslated("lottery.number","{number}", String.valueOf(winningNumber));
       
        if (hasWinners) {
            double jackpot = plugin.getConfig().getDouble("lottery.jackpot",100);
            lotery.setJackpot(jackpot);
            processWinners(name, prize, winners);
        } else {

            lang.broadcastTranslated("lottery.jackpot","{amount}", String.valueOf(lotery.getJackpot()));
            lang.broadcastTranslated("lottery.no_winners","{none}", "");
        }
        lotery.reset();
        saveConfig();
    }
    private List<UUID> getWinners(Lottery lotery){
        List<UUID> winners = new ArrayList<>();

        int winningNumber = lotery.getWinningNumber();

        lotery.getTickets().forEach((player, ticketNumber) -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            String name = offlinePlayer.getName();
            plugin.getLogger().info("Player: " + name + " Ticket: " + ticketNumber);
            for (int ticket : ticketNumber) {
                if (ticket == winningNumber) {
                    plugin.getLogger().info("Player won: " + name + " Ticket: " + ticket);
                    winners.add(player);
                }
            }
        });
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
                    onlinePlayer.sendMessage(lang.getFrom("lottery.won",onlinePlayer.getLocale()).replace("{amount}", String.valueOf(prize)));
                    onlinePlayer.sendMessage(lang.getFrom("lottery.taxes",onlinePlayer.getLocale()).replace("{taxes}", String.valueOf(taxes)));

                }
            }
        }
        List<String> winnersNames = new ArrayList<>();
        for (UUID winner : winners) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(winner);
            winnersNames.add(player.getName());
        }
        lang.broadcastTranslated("lottery.winners","{list}", String.join(", ", winnersNames));
        lang.broadcastTranslated("lottery.reward","{amount}", String.valueOf(prize));

    }
    // Buy a ticket for the lottery
    //#region buy
    public void buyTicket(Player p, String name, int number) {
        Lottery lotery = loteryList.get(name);
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
    //#region Info
    // Get information about a specific lottery
    public void getLoteryInfo(Player p, String name) {
        Lottery lotery = loteryList.get(name);
        if (lotery == null) return;

        p.sendMessage(lang.getFrom("lottery.info.name", p.getLocale()).replace("{name}", name));
        p.sendMessage(lang.getFrom("lottery.info.ticket_price", p.getLocale()).replace("{price}", String.valueOf(lotery.getTicketPrice())));
        p.sendMessage(lang.getFrom("lottery.info.number_limit", p.getLocale()).replace("{numbers}", String.valueOf(lotery.getNumberLimit())));
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
        Lottery lotery = loteryList.get(name);
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
    @Override
    public void run() {
        checkTimeForScheduler();

    }

    private void checkTimeForScheduler() {

        ConfigurationSection scheduler = plugin.getConfig().getConfigurationSection("lottery.draw_scheduler");
        if (scheduler == null) {
            plugin.getLogger().severe("No scheduler section found in config");
            return;
        }

        String currentDay = java.time.LocalDate.now().getDayOfWeek().name().toLowerCase();
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        ConfigurationSection daySection = scheduler.getConfigurationSection(currentDay);
        if (daySection != null) {
            Set<String> times = daySection.getKeys(false);
            for (String time : times) {
                if (time.equals(currentTime)) {
                    List<String> modes = daySection.getStringList(time);
                    for (String mode : modes) {

                        Lottery lotery = loteryList.get(mode);
                        if (lotery != null) {
                            if (lotery.isActive()) {
                                plugin.getLogger().info("Drawing lottery " + mode + " on " + currentDay + " at " + currentTime);
                                drawLotery(mode);
                            }
                        }
                    }
                }
            }
        }
    }
}
