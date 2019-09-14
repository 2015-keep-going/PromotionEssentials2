package me.tekkitcommando.promotionessentials.listener;

import me.tekkitcommando.promotionessentials.PromotionEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDateTime;

public class PlayerLeaveListener implements Listener {

    private PromotionEssentials plugin;

    public PlayerLeaveListener(PromotionEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPluginConfig().getBoolean("time.enabled")) {
            LocalDateTime dateTimeNow = plugin.getDateTimeHandler().getDateTime();

            plugin.getTimes().set(player.getUniqueId().toString() + ".lastLogoff", dateTimeNow.toString());
            plugin.getTimes().set(player.getUniqueId().toString() + ".totalTime", plugin.getTimePromoteHandler().getTotalTime().get(player));
        }
    }
}
