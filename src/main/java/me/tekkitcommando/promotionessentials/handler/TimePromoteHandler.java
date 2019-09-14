package me.tekkitcommando.promotionessentials.handler;

import me.tekkitcommando.promotionessentials.PromotionEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimePromoteHandler {

    private PromotionEssentials plugin;
    private Map<String, String> timedRanks;
    private Map<Player, Long> totalTime = new HashMap<>();

    public TimePromoteHandler(PromotionEssentials plugin) {
        this.plugin = plugin;
        timedRanks = plugin.getPluginConfig().getMap("time.groups");
    }

    public Map<String, String> getTimedRanks() {
        return timedRanks;
    }

    public Map<Player, Long> getTotalTime() {
        return totalTime;
    }

    public int startTimePromote() {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                long totalTime = getTotalTime(player);
                String rankEarned = calculatePromotion(totalTime);
                List<String> noPromoteList = plugin.getPluginConfig().getStringList("time.noPromote");

                if (rankEarned != null) {
                    if (!(noPromoteList.contains(plugin.getPermission().getPrimaryGroup(player)))) {
                        if (!(plugin.getPermission().getPrimaryGroup(player).equalsIgnoreCase(rankEarned))) {
                            plugin.getPromotionHandler().promotePlayer(player, rankEarned);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("PromotedAfterTime")).replace("%group%", rankEarned));
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    private long getTotalTime(Player player) {
        LocalDateTime latestLogin = LocalDateTime.parse(plugin.getTimes().getString(player.getUniqueId().toString() + ".latestLogin"), plugin.getDateTimeHandler().getFormatter());
        long prevTotalTime;
        long ticksToAdd = 0;

        if (!totalTime.containsKey(player)) {
            prevTotalTime = plugin.getTimes().getLong(player.getUniqueId().toString() + ".totalTime");
            totalTime.put(player, prevTotalTime);
        } else {
            prevTotalTime = totalTime.get(player);
        }

        if (plugin.getPluginConfig().getBoolean("time.countOffine")) {
            LocalDateTime lastLogoff = LocalDateTime.parse(plugin.getTimes().getString(player.getUniqueId().toString() + ".lastLogoff"), plugin.getDateTimeHandler().getFormatter());

            if (lastLogoff == null) {
                lastLogoff = latestLogin;
            }

            ticksToAdd += ChronoUnit.SECONDS.between(lastLogoff, latestLogin) * 20;
        }

        LocalDateTime dateTimeNow = plugin.getDateTimeHandler().getDateTime();
        ticksToAdd += ChronoUnit.SECONDS.between(latestLogin, dateTimeNow) * 20;
        totalTime.replace(player, (prevTotalTime + ticksToAdd));

        return totalTime.get(player);
    }

    private String calculatePromotion(long totalTime) {
        String highestRankEarned = null;

        for (String rank : timedRanks.keySet()) {
            long hours;
            long minutes;
            long seconds;

            try {
                hours = Integer.parseInt(timedRanks.get(rank).substring(0, 2));
                minutes = Integer.parseInt(timedRanks.get(rank).substring(3, 5));
                seconds = Integer.parseInt(timedRanks.get(rank).substring(6, 8));
            } catch (NumberFormatException e) {
                plugin.getPluginLogger().warning("Invalid time format! Disabling plugin to prevent corruption of timed ranks!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return null;
            }

            long hoursToSeconds = hours * 3600;
            long minutesToSeconds = minutes * 60;
            long ticksNeededForRank = (hoursToSeconds + minutesToSeconds + seconds) * 20;

            if (ticksNeededForRank <= totalTime) {
                highestRankEarned = rank;
            }
        }

        return highestRankEarned;
    }
}
