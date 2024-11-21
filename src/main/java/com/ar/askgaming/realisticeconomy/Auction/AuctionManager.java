package com.ar.askgaming.realisticeconomy.Auction;

import java.util.HashMap;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class AuctionManager {

    private RealisticEconomy plugin;
    private HashMap<String, Auction> auctions = new HashMap<>();

    public HashMap<String, Auction> getAuctions() {
        return auctions;
    }

    public AuctionManager(RealisticEconomy main) {
        plugin = main;
    }
    
    private boolean maintenance = false;

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    

    
}
