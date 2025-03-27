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

public class LotteryManager extends BukkitRunnable {
    
    private final RealisticEconomy plugin;
    private final File loteryFile;
    private FileConfiguration loteryConfig;

    private HashMap<String, Lottery> loteryList = new HashMap<>();

    public LotteryManager(RealisticEconomy main) {
        plugin = main;

        runTaskTimer(plugin, 0, 20*60);

        loteryFile = new File(plugin.getDataFolder(), "lottery.yml");
        new LotteryCommands();

        load();
    }
    //#region Load
    public void load(){
        if (!loteryFile.exists()) {
            plugin.saveResource("lottery.yml", false);
        }

        loteryConfig = YamlConfiguration.loadConfiguration(loteryFile);

        Set<String> keys = loteryConfig.getKeys(false);
        if (keys.isEmpty()) return;

        for (String key : keys) {
            Object obj = loteryConfig.get(key);
            if (obj instanceof Lottery) {
                Lottery protection = (Lottery) obj;

                loteryList.put(key, protection);
            }
        }
    }

    private void saveConfig() {
        try {
            loteryConfig.save(loteryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getLang(String path, Player p){
        return plugin.getLang().getFrom(path, p);
    }

    public HashMap<String, Lottery> getLoteryList() {
        return loteryList;
    }
    //#region Create
    public void createLotery(String name, int ticketPrice) {

        int maxNumbers = plugin.getConfig().getInt("lottery.max_numbers",50);
        double jackpot = plugin.getConfig().getDouble("lottery.jackpot",100);

        Lottery lotery = new Lottery(name, ticketPrice, maxNumbers, jackpot);

        loteryList.put(name, lotery);
        loteryConfig.set(name, lotery);
        saveConfig();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(getLang("lottery.created", p).replace("{name}", name));
        }
          
    }
    //#region Draw
    public void drawLotery(String name) {
        Lottery lotery = loteryList.get(name);
        if (lotery == null || !lotery.isActive()) return;

        lotery.setActive(false);
        int winningNumber = (int) (Math.random() * lotery.getNumberLimit()) + 1;
        lotery.setWinningNumber(winningNumber);

        List<UUID> winners = getWinners(lotery);
        boolean hasWinners = !winners.isEmpty();
        double prize = hasWinners ? lotery.getJackpot() / winners.size() : 0;

        Bukkit.getPluginManager().callEvent(new LotteryDrawEvent(winningNumber, winners, prize, lotery.getJackpot()));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(getLang("lottery.drawn", p).replace("{name}", name));
            p.sendMessage(getLang("lottery.number", p).replace("{number}", String.valueOf(winningNumber)));
        }
       
        if (hasWinners) {
            double jackpot = plugin.getConfig().getDouble("lottery.jackpot",100);
            lotery.setJackpot(jackpot);
            processWinners(name, prize, winners);

        } else {
        
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(getLang("lottery.jackpot", p).replace("{amount}", String.valueOf(lotery.getJackpot())));
                p.sendMessage(getLang("lottery.no_winners", p));
            }
        }
        lotery.reset();
        saveConfig();
    }
    //#region getWinners
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
    //#region processWinners
    private void processWinners(String name, double prize, List<UUID> winners) {
        List<String> winnersNames = new ArrayList<>();

        for (UUID winner : winners) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(winner);
            winnersNames.add(player.getName());

            boolean succes = plugin.getEconomyService().depositPlayer(winner, prize);
            if (!succes) {
                plugin.getLogger().severe("Error depositing prize to " + player.getName());
                plugin.getEconomyLogger().log("Error depositing prize to " + player.getName() + " in lottery " + name + " amount: " + prize);
                continue;
            }

            double taxes = prize * 0.1;
            plugin.getServerBank().transferWithPlayer(winner, taxes, true);

            //Verificar la transacción

            if (player.isOnline()) {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer != null) {
                    onlinePlayer.sendMessage(getLang("lottery.won",onlinePlayer).replace("{amount}", String.valueOf(prize)));
                    onlinePlayer.sendMessage(getLang("lottery.taxes",onlinePlayer).replace("{taxes}", String.valueOf(taxes)));

                }
            }
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(getLang("lottery.winners", p).replace("{list}", String.join(", ", winnersNames)));
            p.sendMessage(getLang("lottery.reward", p).replace("{amount}", String.valueOf(prize)));
        }

    }
    // Buy a ticket for the lottery
    //#region buy
    public void buyTicket(Player p, String name, int number) {
        Lottery lotery = loteryList.get(name);
        if (lotery == null || !lotery.isActive()) {
            p.sendMessage(getLang("lottery.no_active", p));
            return;
        }
        if (number < 1 || number > lotery.getNumberLimit()) {
            p.sendMessage(getLang("lottery.number_limit", p).replace("{number}", String.valueOf(lotery.getNumberLimit())));
            return;
        }
        if (plugin.getEconomyService().getBalance(p.getUniqueId()) < lotery.getTicketPrice()) {
            p.sendMessage(getLang("error.not_enough", p));
            return;
        }
        if (plugin.getEconomyService().withdrawPlayer(p.getUniqueId(), lotery.getTicketPrice())) {
            lotery.addTicket(p.getUniqueId(), number);
            p.sendMessage(getLang("lottery.buy", p).replace("{number}", String.valueOf(number)));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(getLang("lottery.player_buy", player).replace("{name}", name));
            }

        } else {
            p.sendMessage(getLang("error.transaction", p));
        }
    }
    //#region Info
    // Get information about a specific lottery
    public void getLoteryInfo(Player p, String name) {
        Lottery lotery = loteryList.get(name);
        if (lotery == null) return;

        p.sendMessage(getLang("lottery.info.name", p).replace("{name}", name));
        p.sendMessage(getLang("lottery.info.ticket_price", p).replace("{price}", String.valueOf(lotery.getTicketPrice())));
        p.sendMessage(getLang("lottery.info.number_limit", p).replace("{numbers}", String.valueOf(lotery.getNumberLimit())));
        p.sendMessage(getLang("lottery.info.tickets_sold", p).replace("{sold}", String.valueOf(lotery.getTicketsSold())));
        p.sendMessage(getLang("lottery.info.jackpot", p).replace("{jackpot}", String.valueOf(lotery.getJackpot())));
        p.sendMessage(getLang("lottery.info.active", p).replace("{active}", String.valueOf(lotery.isActive())));

    }
    //#region Delete
    public void deleteLotery(String name) {
        if (loteryList.containsKey(name)) {
            loteryList.remove(name);
            loteryConfig.set(name, null);
            saveConfig();

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(getLang("lottery.remove", p).replace("{name}", name));
            }
        }
    }

    //#region Reset
    public void resetLotery(String name) {
        Lottery lotery = loteryList.get(name);
        if (lotery != null) {
            lotery.reset();

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(getLang("lottery.reset", p).replace("{name}", name));
            }
            saveConfig();
        }
    }

    @Override
    public void run() {
        checkTimeForScheduler();

    }
    //#region Scheduler
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
