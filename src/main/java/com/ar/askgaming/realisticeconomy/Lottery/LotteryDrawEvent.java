package com.ar.askgaming.realisticeconomy.Lottery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.universalnotifier.UniversalNotifier;
import com.ar.askgaming.universalnotifier.Managers.AlertManager.Alert;

public class LotteryDrawEvent extends Event{

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private Integer drawnNumber;
    private Double prize, jackpot;
    private List<UUID> winners;

    public Integer getDrawnNumber() {
        return drawnNumber;
    }

    public List<UUID> getWinners() {
        return winners;
    }
    public Double getJackpot() {
        return jackpot;
    }
    public Double getPrize() {
        return prize;
    }

    public LotteryDrawEvent(Integer winnerNumber, List<UUID> winners, Double prize, Double jackpot) {
        this.drawnNumber = winnerNumber;
        this.winners = winners;
        this.prize = prize;
        this.jackpot = jackpot;

        RealisticEconomy plugin = RealisticEconomy.getInstance();
        
        if (plugin.getServer().getPluginManager().getPlugin("UniversalNotifier") != null) {
            UniversalNotifier notifier = UniversalNotifier.getInstance();
         
            String drawn = plugin.getConfig().getString("notifier.lottery.draw","").replace("%number%", String.valueOf(winnerNumber));
            String extra = "";
       
            if (winners.size() > 0) {
                List<String> winnersNames = new ArrayList<>();
                for (UUID uuid : winners) {
                    winnersNames.add(plugin.getServer().getOfflinePlayer(uuid).getName());
                }
                extra = plugin.getConfig().getString("notifier.lottery.winners","").replace("%winners%", winnersNames.toString()).replace("%prize%", String.valueOf(prize));

            } else {
                extra = plugin.getConfig().getString("notifier.lottery.no_winners","").replace("%jackpot%", String.valueOf(jackpot));
            }

            notifier.getNotificationManager().broadcastToAll(Alert.CUSTOM, drawn + extra);
        } 
    }
}
