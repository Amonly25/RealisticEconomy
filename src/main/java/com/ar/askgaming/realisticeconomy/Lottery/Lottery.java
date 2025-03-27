package com.ar.askgaming.realisticeconomy.Lottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Lottery implements ConfigurationSerializable{
    
    private String name;
    private double ticketPrice, jackpot;
    private int ticketsSold, numberLimit, winningNumber;

    private HashMap<UUID, List<Integer>> tickets = new HashMap<>();

    private boolean isActive;

    public Lottery(String name, int ticketPrice, int numberLimit, Double jackpot) {
        this.name = name;
        this.ticketPrice = ticketPrice;
        this.numberLimit = numberLimit;
        this.ticketsSold = 0;
        this.isActive = true;
        this.jackpot = jackpot;
    }

    public Lottery(Map<String, Object> map){
        this.name = (String) map.get("name");
        this.ticketPrice = (double) map.get("ticketPrice");
        this.ticketsSold = (int) map.get("ticketsSold");
        this.numberLimit = (int) map.get("numberLimit");
        this.winningNumber = (int) map.get("winningNumber");
        this.jackpot = (double) map.get("jackpot");
        this.isActive = (boolean) map.get("isActive");
        if (map.get("tickets") instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, List<Integer>> tickets = (HashMap<String, List<Integer>>) map.get("tickets");
            for (String player : tickets.keySet()) {
                this.tickets.put(UUID.fromString(player), tickets.get(player));
            } 
        }
    }
    //#region Add
    public void addTicket(UUID player, int number) {
        List<Integer> playerTickets = tickets.get(player);
        if (playerTickets == null) {
            playerTickets = new ArrayList<>();
            tickets.put(player, playerTickets);
        }
        playerTickets.add(number);
        tickets.put(player, playerTickets);

        ticketsSold++;
        jackpot += ticketPrice;
    }

    //#region Serialization
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("ticketPrice", ticketPrice);
        map.put("ticketsSold", ticketsSold);
        map.put("numberLimit", numberLimit);
        map.put("winningNumber", winningNumber);
        map.put("jackpot", jackpot);
        map.put("isActive", isActive);
        
        HashMap<String, List<Integer>> tickets = new HashMap<>();
        for (UUID player : this.tickets.keySet()) {
            tickets.put(player.toString(), this.tickets.get(player));
        }
        map.put("tickets", tickets);
        return map;
    }
    //#region Reset
    public void reset(){
        tickets.clear();
        ticketsSold = 0;
        winningNumber = 0;
        isActive = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(int ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public int getNumberLimit() {
        return numberLimit;
    }

    public void setNumberLimit(int numberLimit) {
        this.numberLimit = numberLimit;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public int getWinningNumber() {
        return winningNumber;
    }

    public void setWinningNumber(int winningNumber) {
        this.winningNumber = winningNumber;
    }   
    public HashMap<UUID, List<Integer>> getTickets() {
        return tickets;
    }
    public double getJackpot() {
        return jackpot;
    }

    public void setJackpot(double jacjpot) {
        this.jackpot = jacjpot;
    }
}
