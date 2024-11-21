package com.ar.askgaming.realisticeconomy.Auction;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Auction implements ConfigurationSerializable {

    private Item item;
    private Player owner;
    private double price;
    private int timeLeft;
    private String name;

    private Inventory inventory;

    public Auction(Player owner, double price, int timeLeft, String name) {
        this.owner = owner;
        this.price = price;
        this.timeLeft = timeLeft;
        this.name = name;
    }
    public Inventory getInventory() {
        return inventory;
    }

    public Item getItem() {
        return item;
    }
    public void setItem(Item item) {
        this.item = item;
    }
    public Player getOwner() {
        return owner;
    }
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getTimeLeft() {
        return timeLeft;
    }
    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner.getUniqueId().toString());
        map.put("price", price);
        map.put("timeLeft", timeLeft);
        map.put("name", name);
        return map;
        

    }
}
