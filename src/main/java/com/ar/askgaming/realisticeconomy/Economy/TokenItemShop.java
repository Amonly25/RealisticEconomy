package com.ar.askgaming.realisticeconomy.Economy;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class TokenItemShop {

    private Integer slot;
    private String displayName;
    private List<String> lore;
    private Integer price;
    private List<String> commands;
    private String message;
    private ItemStack item;
    private String broadcast;
    private Boolean giveItem;

    public TokenItemShop(Integer slot, String displayName, List<String> lore, Integer price, List<String> commands, String message, ItemStack item, String broadcast, Boolean giveItem) {
        this.slot = slot;
        this.displayName = displayName;
        this.lore = lore;
        this.price = price;
        this.commands = commands;
        this.message = message;
        this.item = item;
        this.broadcast = broadcast;
        this.giveItem = giveItem;
    }

    public Boolean getGiveItem() {
        return giveItem;
    }
    public void setGiveItem(Boolean giveItem) {
        this.giveItem = giveItem;
    }
    public Integer getSlot() {
        return slot;
    }
    public void setSlot(Integer slot) {
        this.slot = slot;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public List<String>getLore() {
        return lore;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }
    public List<String> getCommands() {
        return commands;
    }
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
    public String getBroadcast() {
        return broadcast;
    }
    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }
    public boolean isGiveItem() {
        return giveItem;
    }
}
