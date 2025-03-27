package com.ar.askgaming.realisticeconomy.Utilities;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolders extends PlaceholderExpansion{

    private final RealisticEconomy plugin;

    public PlaceHolders(){
        this.plugin = RealisticEconomy.getInstance();

        register();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player.getUniqueId());
        if(playerData == null){
            return "0";
        }
        
        switch (identifier) {
            case "balance":
                return String.valueOf(playerData.getBalance());
            case "bank_balance":
                return String.valueOf(playerData.getBankBalance());
            case "tokens":
                return String.valueOf(playerData.getTokens());
            case "debt":
                return String.valueOf(playerData.getDebt());
            case "is_seized":
                return String.valueOf(playerData.isSeizedAccount());
        
            default:
                return "Invalid Placeholder";
        }
    }

    @Override
    public @NotNull String getAuthor() {
        return "AskGaming";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "realisticeconomy";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    
}
