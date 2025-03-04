package com.ar.askgaming.realisticeconomy.Economy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class TokenShop implements Listener{

    private RealisticEconomy plugin;
    private final File file;
    private FileConfiguration config;
    private final String name;

    private HashMap<Integer, Inventory> inventories = new HashMap<>();
    private HashMap<Integer, TokenItemShop> slotItemMap = new HashMap<>();
    private List<ItemStack> itemsSize = new ArrayList<>();

    public TokenShop(RealisticEconomy plugin) {
        this.plugin = plugin;
        this.name = plugin.getConfig().getString("tokenshop.title").replace('&', '§');
        this.file = new File(plugin.getDataFolder(), "tokenshop.yml");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        loadConfig();        

    }
    public void clearInventories() {
        inventories.forEach((k, v) -> {
            // Crea una copia de los viewers y cierra sus inventarios
            List<HumanEntity> viewers = new ArrayList<>(v.getViewers());
            viewers.forEach(HumanEntity::closeInventory);
            // Luego limpia el inventario
            v.clear();
        });
    }

    public void loadConfig() {
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadItemsFromConfig();
    }

    private void loadItemsFromConfig() {
        inventories.clear();
        itemsSize.clear();
        for (Inventory inv : inventories.values()) {
            inv.clear();
        }
        List<String> keys = new ArrayList<>(config.getKeys(false));
        for (String key : keys) {
            int slot = Integer.parseInt(key);
            String displayName = config.getString(key + ".displayName", "Invalid Name");
            List<String> lore = config.getStringList(key + ".lore");
            int price = config.getInt(key + ".price", 0);
            List<String> commands = config.getStringList(key + ".commands");
            String message = config.getString(key + ".message", "");
            String broadcast = config.getString(key + ".broadcast", "");
            ItemStack item = config.getItemStack(key + ".item", new ItemStack(Material.STONE));
            Boolean giveItem = config.getBoolean(key + ".giveItem", false);
            TokenItemShop tokenItemShop = new TokenItemShop(slot, displayName, lore, price, commands, message, item, broadcast, giveItem);
            slotItemMap.put(slot, tokenItemShop);
            itemsSize.add(updateNameAndLore(item, displayName, lore));
        }
        createInventories();
    }
    private ItemStack updateNameAndLore(ItemStack original, String name, List<String> lore) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> newLore = new ArrayList<>();

        if (meta.hasLore()){
            for (String line : meta.getLore()) {
                newLore.add(line);
            }
        }
        for (String line : lore) {
            newLore.add(line.replace('&', '§'));
        }
        meta.setDisplayName(name.replace('&', '§'));
        meta.setLore(newLore);
        item.setItemMeta(meta);
        return item;
    }
    //#region create
    public void createNew(ItemStack item){
        itemsSize.add(item);
        int slot = itemsSize.size() - 1;
        String key = slot + "";
        config.set(key + ".displayName", item.getItemMeta().getDisplayName());
        config.set(key + ".lore", item.getItemMeta().hasLore() ? item.getItemMeta().getLore().toArray(new String[0]) : new ArrayList<>());
        config.set(key + ".price", 0);
        config.set(key + ".commands", new ArrayList<>());
        config.set(key + ".message", "");
        config.set(key + ".broadcast", "");
        config.set(key + ".item", item);
        config.set(key + ".giveItem", false);
        saveConfig();
        addItemToInventory(item);
        //slotItemMap.put(slot, new TokenItemShop(slot, item.getItemMeta().getDisplayName(), item.getItemMeta().getLore().toArray(new String[0]), 0, new ArrayList<>(), "", item, "", false));
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createInventories() {
        int size = itemsSize.size();
        int pages = (size + 44) / 45;
        for (int i = 0; i < pages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, name + " " + (i + "/"+ 1));
            inventories.put(i, inv);
            addNavigationButtons(inv);
        }
        distributeItems();
    }

    private void distributeItems() {
        for (int i = 0, j = 0; i < itemsSize.size(); i++, j++) {
            if (j % 54 == 45 || j % 54 == 53) {
                j++;
            }
            int page = j / 54;
            inventories.get(page).setItem(j % 54, itemsSize.get(i));
        }
    }

    private void addNavigationButtons(Inventory inv) {
        inv.setItem(45, createNavigationButton("Previous Page", Material.ARROW));
        inv.setItem(53, createNavigationButton("Next Page", Material.ARROW));
    }

    private ItemStack createNavigationButton(String name, Material material) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    public boolean isShopInventory(Inventory inv) {
        return inventories.containsValue(inv);
    }

    @EventHandler
    public void handleNavigationClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        if (!isShopInventory(event.getClickedInventory())) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot == 45) {
            openInventory(player, getCurrentPage(player) - 1);
        } else if (slot == 53) {
            openInventory(player, getCurrentPage(player) + 1);
        }
    }

    public void openInventory(Player player, int page) {
        if (inventories.isEmpty()) {
            player.sendMessage("§cNo items to show in the shop.");
            return;
        }
        if (page >= 0 && page < inventories.size()) {
            player.openInventory(inventories.get(page));
        }
    }
    public void addItemToInventory(ItemStack item) {

        for (Inventory inv : inventories.values()) {
            if (inv.firstEmpty() != -1 && inv.firstEmpty() != 45 && inv.firstEmpty() != 53) {
                inv.addItem(item);
                return;
            }
        }
        Inventory newInv = Bukkit.createInventory(null, 54, name + (inventories.size() + 1));
        inventories.put(inventories.size(), newInv);
        addNavigationButtons(newInv);

        newInv.addItem(item);
    }
    private int getCurrentPage(Player player) {
        for (Map.Entry<Integer, Inventory> entry : inventories.entrySet()) {
            if (entry.getValue().equals(player.getOpenInventory().getTopInventory())) {
                return entry.getKey();
            }
        }
        return 0;
    }
    public FileConfiguration getConfig() {
        return config;
    }
    //#region buy
    public void proceesBuy(Player p, Integer slot, ItemStack item) {
        UUID uuid = p.getUniqueId();
        int tokens = plugin.getEconomyService().getTokens(uuid);

        if (slotItemMap.containsKey(slot)){
            TokenItemShop tokenItemShop = getItemShop(slot);
            int price = tokenItemShop.getPrice();
            boolean canContinue = false;

            if (price > 0){
                if (tokens < price){ 
                    p.sendMessage(plugin.getLang().getFrom("token.not_enough", p));
                    return;
                }
                canContinue = plugin.getEconomyService().setTokens(uuid, tokens - price);
            }
            if (canContinue){
                plugin.getEconomyLogger().log("Player " + p.getName() + " bought " + tokenItemShop.getDisplayName() + " for " + price + " tokens.");
                if (tokenItemShop.getCommands().size() > 0){
                    for (String command : tokenItemShop.getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", p.getName()));
                    }
                }
                if (tokenItemShop.getMessage().length() > 0){
                    p.sendMessage(tokenItemShop.getMessage().replace('&', '§'));
                }
                if (tokenItemShop.getBroadcast().length() > 0){
                    Bukkit.broadcastMessage(tokenItemShop.getBroadcast().replace("{player}", p.getName()).replace('&', '§'));
                }
                if (tokenItemShop.isGiveItem()){
                    p.getInventory().addItem(tokenItemShop.getItem());
                }
            }
        }
    }
    private TokenItemShop getItemShop(int slot) {
        return slotItemMap.get(slot);
    }
    //#region getters
    public HashMap<Integer, TokenItemShop> getSlotItemMap() {
        return slotItemMap;
    }
    public HashMap<Integer, Inventory> getInventories() {
        return inventories;
    }
}
