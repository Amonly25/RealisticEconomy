package com.ar.askgaming.realisticeconomy;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.datas.DatabaseManager;

public class EconomyTransactions {

    private DatabaseManager database;
    private RealisticEconomy plugin;

    public EconomyTransactions(RealisticEconomy main) {
        plugin = main;
        database = plugin.getDatabase();
    }
    private String getMsg(String key){
        return plugin.getLang().getFrom(key, plugin.getServerLang());
    }
    private void log(String key){
        plugin.getEconomyLogger().log(key);
    }
    
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
    public int getTokens(UUID player) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData != null) {
            return playerData.getTokens();
        }
        return 0;
    }
    public boolean setTokens(UUID player, int amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setTokens(amount);
        return playerData.save();
    }
    public boolean addTokens(UUID player, int amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setTokens(playerData.getTokens() + amount);
        return playerData.save();
    }
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
    public double getDebt(UUID player) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData != null) {
            return playerData.getDebt();
        }
        return 0;
    }
    public boolean setDebt(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setDebt(amount);
        return playerData.save();
    }
    public boolean addDebt(UUID player, double amount) {
        PlayerData playerData = plugin.getDatabase().loadPlayerData(player);
        if (playerData == null) {
            return false;
        }
        playerData.setDebt(playerData.getDebt() + amount);
        return playerData.save();
    }
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

    public boolean hasAccount(UUID player) {
        return plugin.getDatabase().loadPlayerData(player) != null;
    }
    public boolean hasAccount(Player player) {
        return hasAccount(player.getUniqueId());
    }



    // //#region setBalance
    // public EconomyResponse setPlayerBalance(UUID player, double amount){

    //     if (amount < 0) {
    //         return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
    //     }
    //     OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(player);
    //     Connection conn = null;
    //     try {
    //         conn = database.connect();
    //         conn.setAutoCommit(false); // Iniciar la transacción
    
    //         // Insertar o ignorar el jugador en la tabla economy
    //         String insertSql;
    //         if (database.getDatabaseType().equalsIgnoreCase("sqlite")) {
    //             insertSql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
    //         } else {
    //             insertSql = "INSERT INTO economy (uuid, balance) VALUES (?, 0) ON DUPLICATE KEY UPDATE uuid=uuid";
    //         }
    
    //         try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
    //             stmt.setString(1, player.toString());
    //             stmt.executeUpdate();
    //         }
    
    //         // Actualizar el balance del jugador
    //         String updateSql = "UPDATE economy SET balance = ? WHERE uuid = ?";
    //         try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
    //             stmt.setDouble(1, amount);
    //             stmt.setString(2, player.toString());
    //             stmt.executeUpdate();
    //         }
    
    //         conn.commit(); // Confirmar la transacción
    
    //         if (offPlayer.isOnline()) {
    //             offPlayer.getPlayer().sendMessage(getMsg("economy.set_receiver").replace("%amount%", String.valueOf(amount)));
    //         }
    
    //         String s = getMsg("economy.set_player").replace("%amount%", String.valueOf(amount)).replace("%player%", offPlayer.getName());
    //         log(s);
    //         return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, s);
    
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         if (conn != null) {
    //             try {
    //                 conn.rollback(); // Revertir la transacción en caso de error
    //             } catch (SQLException rollbackEx) {
    //                 rollbackEx.printStackTrace();
    //             }
    //         }
    //     } finally {
    //         if (conn != null) {
    //             try {
    //                 conn.close(); // Asegurarse de cerrar la conexión
    //             } catch (SQLException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    
    //     return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Failed to set balance");
    // }

    // //#region depositPlayer
    // public EconomyResponse depositPlayer(UUID uuid, double amount) {
    //     String data = uuid.toString();
    //     OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
        
    //     if (amount < 0) {
    //         return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
    //     }
    
    //     Connection conn = null;  // Declaramos conn aquí para usarlo en todo el método
    
    //     try {
    //         conn = database.connect();
    //         conn.setAutoCommit(false); // Iniciar la transacción
    
    //         // Insertar o ignorar el jugador en la tabla economy
    //         String insertSql;
    //         if (database.getDatabaseType().equalsIgnoreCase("sqlite")) {
    //             insertSql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
    //         } else {
    //             insertSql = "INSERT INTO economy (uuid, balance) VALUES (?, 0) ON DUPLICATE KEY UPDATE uuid=uuid";
    //         }
    
    //         try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
    //             stmt.setString(1, data);
    //             stmt.executeUpdate();
    //         }
    
    //         // Actualizar el balance del jugador
    //         String updateSql = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";
    //         try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
    //             stmt.setDouble(1, amount);
    //             stmt.setString(2, data);
    //             stmt.executeUpdate();
    //         }
    
    //         conn.commit(); // Confirmar la transacción
    
    //         // Enviar mensaje al jugador si está en línea
    //         if (offPlayer.isOnline()) {
    //             offPlayer.getPlayer().sendMessage(getMsg("server_bank.deposit_receiver").replace("%amount%", String.valueOf(amount)));
    //         }
    
    //         // Log del evento
    //         String s = getMsg("server_bank.deposit_player").replace("%amount%", String.valueOf(amount)).replace("%player%", offPlayer.getName());
    //         log(s);
    //         return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, s);
    
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         if (conn != null) {
    //             try {
    //                 conn.rollback(); // Revertir la transacción en caso de error
    //             } catch (SQLException rollbackEx) {
    //                 rollbackEx.printStackTrace();
    //             }
    //         }
    //     } finally {
    //         if (conn != null) {
    //             try {
    //                 conn.close(); // Asegurarse de cerrar la conexión
    //             } catch (SQLException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    
    //     return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%", "Player Deposit"));
    // }
    
    // public EconomyResponse depositPlayer(Player player, double amount) {
    //     return depositPlayer(player.getUniqueId(), amount);
    // }

    // public EconomyResponse depositPlayer(String playerName, double amount) {
    //     Player p = Bukkit.getPlayerExact(playerName);
    //     if (p != null){
    //         return depositPlayer(p.getUniqueId(), amount);
    //     } else {
    //         @SuppressWarnings("deprecation")
    //         OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
    //         if (offPlayer.hasPlayedBefore()) {
    //             return depositPlayer(offPlayer.getUniqueId(), amount);
    //         } else {
    //             return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //         }
    //     }
    // }
    // public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
    //     if (offlinePlayer.hasPlayedBefore()){
    //         return depositPlayer(offlinePlayer.getUniqueId(), amount);
    //     }else {
    //         return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //     }
        
    // }

    // //#endregion

    // //#region getBalance
    // public double getBalance(UUID uuid) {
    //     try (Connection conn = database.connect()) {
    //         String sql = "SELECT balance FROM economy WHERE uuid = ?";
    //         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    //             stmt.setString(1, uuid.toString());
    //             ResultSet rs = stmt.executeQuery();
    //             if (rs.next()) {
    //                 return rs.getDouble("balance");
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return 0.0; // Retorna 0 si no se encuentra el jugador
    // }
    // public double getBalance(Player player) {
    //     return getBalance(player.getUniqueId());
    // }

    // public double getBalance(String playerName) {
    //     Player p = Bukkit.getPlayerExact(playerName);
    //     if (p != null){
    //         return getBalance(p.getUniqueId());
    //     } else {
    //         @SuppressWarnings("deprecation")
    //         OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
    //         if (offPlayer.hasPlayedBefore()) {
    //             return getBalance(offPlayer.getUniqueId());
    //         } else {
    //             return 0.0;
    //         }
    //     }
    // }

    // public double getBalance(OfflinePlayer offlinePlayer) {
    //     if (offlinePlayer.hasPlayedBefore()) {
    //         return getBalance(offlinePlayer.getUniqueId());
    //     } else {
    //         return 0;
    //     }
    // }

    // public boolean has(String playerName, double amount) {
    //     return getBalance(playerName) >= amount;
    // }

    // public boolean has(OfflinePlayer arg0, double arg1) {
    //     return getBalance(arg0) >= arg1;
    // }

    // //#endregion

    // //#region withdrawPlayer
    // public EconomyResponse withdrawPlayer(UUID uuid, double amount) {
    //     OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
    //     double balance = getBalance(uuid);
    //     if (balance >= amount) {
    //         try (Connection conn = database.connect()) {
    //             String updateSql = "UPDATE economy SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
    //             try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
    //                 stmt.setDouble(1, amount);
    //                 stmt.setString(2, uuid.toString());
    //                 stmt.setDouble(3, amount);
    //                 int rowsUpdated = stmt.executeUpdate();
    //                 if (rowsUpdated > 0) {
    //                     if (offPlayer.isOnline()){
    //                         offPlayer.getPlayer().sendMessage(getMsg("server_bank.withdraw_receiver").replace("%amount%", String.valueOf(amount)));
    //                     }
    //                     String s = getMsg("server_bank.withdraw_player").replace("%amount%", String.valueOf(amount)).replace("%player%", offPlayer.getName());
    //                     log(s);
    //                     return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, s);
    //                 } else {
    //                     return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Withdraw Database"));
    //                 }
    //             }
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //             return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Withdraw"));
    //         }
    //     } else {
    //         return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_enough"));
    //     }
    // }

    // public EconomyResponse withdrawPlayer(String playerName, double amount) {
    //     Player p = Bukkit.getPlayerExact(playerName);
    //     if (p != null){
    //         return withdrawPlayer(p.getUniqueId(), amount);
    //     } else {
    //         @SuppressWarnings("deprecation")
    //         OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
    //         if (offPlayer.hasPlayedBefore()) {
    //             return withdrawPlayer(offPlayer.getUniqueId(), amount);
    //         } else {
    //             return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //         }
    //     }
    // }

    // public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
    //     if (offlinePlayer.hasPlayedBefore()){
    //         return withdrawPlayer(offlinePlayer.getUniqueId(), amount);
    //     }else {
    //         return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //     }
    // }

    // //#endregion

    // //#region hasAccount
    // public boolean hasAccount(String playerName) {
    //     Player p = Bukkit.getPlayerExact(playerName);
    //     if (p != null){
    //         return p.hasPlayedBefore();
    //     } else {
    //         @SuppressWarnings("deprecation")
    //         OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
    //         return offPlayer.hasPlayedBefore();
    //     }
    // }

    // public boolean hasAccount(OfflinePlayer arg0) {
    //     return arg0.hasPlayedBefore();
    // }

    // //#endregion
    // //#region playerPayPlayer
    // public EconomyResponse playerPayPlayer(UUID payer, UUID receiver, double amount) {
    //     if (amount < 0) {
    //         return new EconomyResponse(0, getBalance(payer), EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
    //     }
    
    //     double payerBalance = getBalance(payer);
    
    //     if (payerBalance < amount) {
    //         return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.payer_not_enough"));
    //     }
    
    //     try (Connection conn = database.connect()) {
    //         conn.setAutoCommit(false); // Iniciar la transacción
    
    //         try {
    //             // Asegurar que el receptor existe en la base de datos
    //             String ensureReceiverSql = database.getDatabaseType().equals("MYSQL") 
    //                     ? "INSERT IGNORE INTO economy (uuid, balance) VALUES (?, 0)"  // MySQL
    //                     : "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)"; // SQLite
    //             try (PreparedStatement ensureStmt = conn.prepareStatement(ensureReceiverSql)) {
    //                 ensureStmt.setString(1, receiver.toString());
    //                 ensureStmt.executeUpdate();
    //             }
    
    //             // Actualizar el saldo del pagador
    //             String withdrawSql = "UPDATE economy SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
    //             try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql)) {
    //                 withdrawStmt.setDouble(1, amount);
    //                 withdrawStmt.setString(2, payer.toString());
    //                 withdrawStmt.setDouble(3, amount);
    //                 int rowsUpdated = withdrawStmt.executeUpdate();
    //                 if (rowsUpdated == 0) {
    //                     conn.rollback();
    //                     return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%", "Player Withdraw Database"));
    //                 }
    //             }
    
    //             // Actualizar el saldo del receptor
    //             String depositSql = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";
    //             try (PreparedStatement depositStmt = conn.prepareStatement(depositSql)) {
    //                 depositStmt.setDouble(1, amount);
    //                 depositStmt.setString(2, receiver.toString());
    //                 depositStmt.executeUpdate();
    //             }
    
    //             conn.commit(); // Finalizar la transacción
    
    //             double newPayerBalance = getBalance(payer);
    //             if (Bukkit.getOfflinePlayer(receiver).isOnline()) {
    //                 Bukkit.getPlayer(receiver).sendMessage(getMsg("transactions.pay_other").replace("%amount%", String.valueOf(amount)).replace("%player%", Bukkit.getPlayer(payer).getName()));
    //             }
    //             String s = getMsg("transactions.pay_success").replace("%amount%", String.valueOf(amount)).replace("%receiver%", Bukkit.getOfflinePlayer(receiver).getName()).replace("%payer%", Bukkit.getOfflinePlayer(payer).getName());
    //             log(s);
    //             return new EconomyResponse(amount, newPayerBalance, EconomyResponse.ResponseType.SUCCESS, s);
    
    //         } catch (SQLException e) {
    //             conn.rollback(); // Revertir la transacción en caso de error
    //             e.printStackTrace();
    //             return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%", "Player Pay Player Database rollback"));
    //         } finally {
    //             conn.setAutoCommit(true); // Restaurar el modo de autocommit
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%", "Player Pay Player Database"));
    //     }
    // }
    

    // public EconomyResponse playerPayPlayer(Player payer, String receiverName, double amount) {

    //     @SuppressWarnings("deprecation")
    //     OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverName);
    //     if (receiver.hasPlayedBefore()) {
    //         return playerPayPlayer(payer.getUniqueId(), receiver.getUniqueId(), amount);
    //     } else {
    //         return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //     }
    // }
    // @SuppressWarnings("deprecation")
    // public EconomyResponse playerPayPlayer(String payerName, String receiverName, double amount) {

    //     OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverName);
    //     OfflinePlayer payer = Bukkit.getOfflinePlayer(payerName);
    //     if (receiver.hasPlayedBefore() && payer.hasPlayedBefore()) {
    //         return playerPayPlayer(payer.getUniqueId(), receiver.getUniqueId(), amount);
    //     } else {
    //         return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
    //     }
    // }
    ////#endregion
}
