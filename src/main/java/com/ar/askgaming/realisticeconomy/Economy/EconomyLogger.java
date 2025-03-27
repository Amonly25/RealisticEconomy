package com.ar.askgaming.realisticeconomy.Economy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class EconomyLogger {

    private boolean enabled;
    private File logFile;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }
    private RealisticEconomy plugin;
    public EconomyLogger(RealisticEconomy main) {
        plugin = main;
        enabled = main.getConfig().getBoolean("log_economy", true);

        logFile = new File(plugin.getDataFolder() + "/logs/logs.txt");
        if (!logFile.exists()) {
            plugin.saveResource("logs/logs.txt", false);
            
        }
    }
    public void log(String message) {
        if (enabled) {
            
            try (FileWriter fw = new FileWriter(logFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
                String timestamp = java.time.LocalDateTime.now().toString();
                out.println("[" + timestamp + "] " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
