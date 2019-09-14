package me.tekkitcommando.promotionessentials.listener;

import me.tekkitcommando.promotionessentials.PromotionEssentials;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class PlayerJoinListener implements Listener {

    private PromotionEssentials plugin;

    public PlayerJoinListener(PromotionEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<String> groups = Arrays.asList(plugin.getPermission().getPlayerGroups(player));

        if (plugin.getPluginConfig().getBoolean("apply.enabled")) {
            if (!(groups.contains(plugin.getPluginConfig().getString("apply.promotion"))) && groups.contains(plugin.getPluginConfig().getString("apply.default"))) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("Join").replace("%player%", player.getName())));
            }
        }

        if (plugin.getPluginConfig().getBoolean("time.enabled")) {
            if (!plugin.getTimes().contains(player.getUniqueId().toString())) {
                LocalDateTime dateTimeNow = plugin.getDateTimeHandler().getDateTime();

                plugin.getTimes().set(player.getUniqueId().toString() + ".totalTime", 0);
                plugin.getTimes().set(player.getUniqueId().toString() + ".firstJoin", dateTimeNow.toString());
                plugin.getTimes().set(player.getUniqueId().toString() + ".latestLogin", dateTimeNow.toString());
            } else {
                LocalDateTime dateTimeNow = plugin.getDateTimeHandler().getDateTime();

                plugin.getTimes().set(player.getUniqueId().toString() + ".latestLogin", dateTimeNow.toString());
            }
        }
    }
}