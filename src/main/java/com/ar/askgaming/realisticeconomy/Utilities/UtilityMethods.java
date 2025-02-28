package com.ar.askgaming.realisticeconomy.Utilities;

import java.util.List;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class UtilityMethods {

    private RealisticEconomy plugin;
    public UtilityMethods(RealisticEconomy plugin) {
        this.plugin = plugin;
    }

    public void listToPage(List<String> list, String[] args, Player p) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(plugin.getLang().getFrom("error.invalid_amount",p));
                return;
            }
        }

        int totalPages = (int) Math.ceil(list.size() / 10.0);
        if (page > totalPages || page < 1) {
            p.sendMessage(plugin.getLang().getFrom("error.invalid_amount", p));
            return;
        }

        int start = (page - 1) * 10;
        int end = Math.min(start + 10, list.size());
        p.sendMessage(plugin.getLang().getFrom("misc.pages", p) + " " + page + "/" + totalPages);
        for (int i = start; i < end; i++) {
            String clan = list.get(i);
            p.sendMessage((i + 1) + ". " + clan);
        }
    }
}
