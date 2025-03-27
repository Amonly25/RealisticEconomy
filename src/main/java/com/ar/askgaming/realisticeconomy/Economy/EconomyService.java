package com.ar.askgaming.realisticeconomy.Economy;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class EconomyService {

    private final RealisticEconomy plugin;

    public EconomyService(RealisticEconomy main) {
        plugin = main;
    }

    //#region Economy
    /**
     * Sets the balance of a player.
     *
     * @param player The UUID of the player.
     * @param amount The new balance amount.
     * @return true if the balance was successfully set, false otherwise.
     */
    public boolean setPlayerBalance(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setBalance(amount);
        return playerData.save();
    }
    
    public boolean setPlayerBalance(Player player, double amount) {
        return setPlayerBalance(player.getUniqueId(), amount);
    }
    //#region deposit
    public boolean depositPlayer(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setBalance(playerData.getBalance() + amount);
        return playerData.save();
    }
    
    public boolean depositPlayer(Player player, double amount) {
        return depositPlayer(player.getUniqueId(), amount);
    }
    //#region withdraw
    public boolean withdrawPlayer(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        if (playerData.getBalance() >= amount) {
            playerData.setBalance(playerData.getBalance() - amount);
            return playerData.save();
        }
        return false;
    }
    
    public double getBalance(UUID player) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData != null) {
            return playerData.getBalance();
        }
        return 0;
    }
    
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }
    //#region pay
    /**
     * Sets the balance of a player.
     *
     * @param payer The UUID of the payer
     * @param receiver The UUID of the receiver
     * @param amount The amount to transfer
     * @return true if the balance was successfully set, false otherwise.
     */
    public boolean playerPayPlayer(UUID payer, UUID receiver, double amount) {
        if (amount < 0) {
            return false;
        }
        if (getBalance(payer) >= amount) {
            if (withdrawPlayer(payer, amount)) {
                if (depositPlayer(receiver, amount)) {
                    return true;
                } else {
                    // Revertir la transacción si el depósito al receptor falla
                    depositPlayer(payer, amount);
                }
            }
        }
        return false;
    }

    //#region PlayerBank
    public double getDebt(UUID player) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData != null) {
            return playerData.getDebt();
        }
        return 0;
    }
    //#region set
    public boolean setDebt(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setDebt(amount);
        return playerData.save();
    }
    //#region add
    public boolean addDebt(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setDebt(playerData.getDebt() + amount);
        return playerData.save();
    }
    //#region take
    public boolean takeDebt(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        if (playerData.getDebt() >= amount) {
            playerData.setDebt(playerData.getDebt() - amount);
            return playerData.save();
        }
        return false;
    }
    //#region deposit
    public boolean depositToPlayerBank(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setBankBalance(playerData.getBankBalance() + amount);
        playerData.setBalance(playerData.getBalance() - amount);
        if (playerData.save()) {
            return true;
        } else{
            playerData.setBankBalance(playerData.getBankBalance() - amount);
            playerData.setBalance(playerData.getBalance() + amount);
            return false;
        }
    }
    public boolean depositToPlayerBank(Player player, double amount) {
        return depositToPlayerBank(player.getUniqueId(), amount);
    }
    //#region withdraw
    public boolean withdrawFromPlayerBank(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        if (playerData.getBankBalance() >= amount) {
            playerData.setBankBalance(playerData.getBankBalance() - amount);
            playerData.setBalance(playerData.getBalance() + amount);
            if (playerData.save()) {
                return true;
            } else{
                playerData.setBankBalance(playerData.getBankBalance() + amount);
                playerData.setBalance(playerData.getBalance() - amount);
                return false;
            }
        }
        return false;
    }
    //#region Tokens
    public int getTokens(UUID player) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData != null) {
            return playerData.getTokens();
        }
        return 0;
    }
    //#region set
    public boolean setTokens(UUID player, int amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setTokens(amount);
        return playerData.save();
    }
    //#region add
    public boolean addTokens(UUID player, int amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setTokens(playerData.getTokens() + amount);
        return playerData.save();
    }
    //#region remove
    public boolean removeTokens(UUID player, int amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        if (playerData.getTokens() >= amount) {
            playerData.setTokens(playerData.getTokens() - amount);
            return playerData.save();
        }
        return false;
    }
    //#region pay
    /**
     * Sets the balance of a player.
     *
     * @param payer The UUID of the player.
     * @param receiver The UUID of the player.
     * @param amount The amount
     * @return true if the balance was successfully set, false otherwise.
     */
    public boolean playerPayTokenToPlayer(UUID payer, UUID receiver, int amount) {
        if (amount < 0) {
            return false;
        }
        if (getTokens(payer) >= amount) {
            if (removeTokens(payer, amount)) {
                if (addTokens(receiver, amount)) {
                    return true;
                } else {
                    return addTokens(payer, amount);
                }
            }
        }
        return false;
    }
}
