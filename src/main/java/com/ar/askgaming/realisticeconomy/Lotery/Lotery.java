package com.ar.askgaming.realisticeconomy.Lotery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Lotery implements ConfigurationSerializable{
    
    private String name;
    private double ticketPrice;
    private int ticketsSold;
    private int numberLimit;
    private int winningNumber;
    private double jackpot;

    private HashMap<UUID, Integer> tickets = new HashMap<>();

    private boolean isActive;

    public Lotery(String name, int ticketPrice, int numberLimit) {
        this.name = name;
        this.ticketPrice = ticketPrice;
        this.numberLimit = numberLimit;
        this.ticketsSold = 0;
        this.isActive = true;
        this.jackpot = 0;
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
    public HashMap<UUID, Integer> getTickets() {
        return tickets;
    }
    public double getJackpot() {
        return jackpot;
    }

    public void setJackpot(double jacjpot) {
        this.jackpot = jacjpot;
    }

    public void addTicket(UUID player, int number) {
        tickets.put(player, number);
        ticketsSold++;
        jackpot += ticketPrice;
    }
    public Lotery(Map<String, Object> map){
        this.name = (String) map.get("name");
        this.ticketPrice = (double) map.get("ticketPrice");
        this.ticketsSold = (int) map.get("ticketsSold");
        this.numberLimit = (int) map.get("numberLimit");
        this.winningNumber = (int) map.get("winningNumber");
        this.jackpot = (double) map.get("jackpot");
        this.isActive = (boolean) map.get("isActive");
        this.tickets = (HashMap<UUID, Integer>) map.get("tickets");
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
        map.put("tickets", tickets);
        return map;
    }
}
