package com.ar.askgaming.realisticeconomy.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class InventoryClickListener implements Listener{

    private RealisticEconomy plugin;

    public InventoryClickListener(RealisticEconomy main) {
        plugin = main;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        Inventory clicked = e.getClickedInventory();

        if (plugin.getTokenShop().isShopInventory(inv)) {
            e.setCancelled(true);
        }
        if (!plugin.getTokenShop().isShopInventory(clicked)){
            return;
        }

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        int slot = e.getSlot();
        if (slot == 45 || slot == 53) {
            return;
        }
        if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.LEFT) {
            if (e.getWhoClicked() instanceof Player) {
                Player p = (Player) e.getWhoClicked();
                plugin.getTokenShop().proceesBuy(p,slot, item);
            }

        }
    }
}
