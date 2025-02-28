package com.ar.askgaming.realisticeconomy.Utilities;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;
import com.ar.askgaming.realisticeconomy.Economy.EconomyManager;

public class TimeManager {
    
    private RealisticEconomy plugin;

    public TimeManager(RealisticEconomy main) {
        plugin = main;
    }

    private String getLang(String path, Player p){
        return plugin.getLang().getFrom(path, p);
    }

    public void checkPlayer(Player p) {
        
        // Cargar los datos del jugador desde la base de datos
        PlayerData pd = plugin.getDatabase().loadPlayerData(p.getUniqueId());

        if (pd.isSeizedAccount()) {
            p.sendMessage(getLang("bank.seized_account", p));
            return;
        }
    
        // Obtener el tiempo de la última conexión y el tiempo actual
        long lastConnected = pd.getLastConnected();
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastConnected;
        long timeDifferenceInDays = timeDifference / (1000 * 60 * 60 * 24); // Convertir la diferencia de tiempo a días

        // Si ha pasado al menos un día desde la última conexión
        if (timeDifferenceInDays > 0) {
            // Calcular el interés sobre el saldo bancario del jugador
            double balance = pd.getBankBalance();
            double interestRate = plugin.getEconomyManager().getSavingsInterest();
            double deposit = (balance * interestRate /100) * timeDifferenceInDays;
            deposit = EconomyManager.formatDouble(deposit);
            // Verificar si el banco del servidor tiene suficiente saldo para pagar el interés
            if (plugin.getServerBank().getBalance() < deposit) {
                deposit = plugin.getServerBank().getBalance();
                p.sendMessage(getLang("bank.no_bank_enought", p));
            }
            
            // Transferir el interés al jugador
            if (deposit > 0) {
                if (plugin.getServerBank().withdrawFromServerToPlayer(p.getUniqueId(), deposit)) {
                    p.sendMessage(getLang("bank.interest", p).replace("{amount}", String.valueOf(deposit)));
                    plugin.getEconomyLogger().log("Interest of " + deposit + " has been paid to " + p.getName());
                } else {
                    p.sendMessage(getLang("error.transaction", p));
                }
            }
            
            // Calcular el interés sobre la deuda del jugador
            double loan = pd.getDebt();
            double interestRateLoan = plugin.getEconomyManager().getLoanInterest()/100;
            double interest = loan * interestRateLoan * timeDifferenceInDays;
            interest = EconomyManager.formatDouble(interest);
            // Si hay interés sobre la deuda
            if (interest > 0) {
                plugin.getEconomyLogger().log("Interest of " + interest + " has been added to " + p.getName() + "'s debt");
                p.sendMessage(getLang("bank.loan_interest", p).replace("{amount}", interest + ""));
                pd.setDebt(interest + loan); // Actualizar la deuda del jugador
                pd.checkSeizedAccount(); // Verificar si la cuenta del jugador debe ser embargada
                if (pd.isSeizedAccount()) {
                    plugin.getLogger().info("Account of " + p.getName() + " has been seized");
                    p.sendMessage(getLang("bank.seized_account", p));
                }
                pd.save(); // Guardar los datos del jugador
            }
        }
        pd.setLastConnected(currentTime); // Actualizar el tiempo de la última conexión
        pd.save(); // Guardar los datos del jugador
    }
}
