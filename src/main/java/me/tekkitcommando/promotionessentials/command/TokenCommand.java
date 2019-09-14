package me.tekkitcommando.promotionessentials.command;

import me.tekkitcommando.promotionessentials.PromotionEssentials;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TokenCommand implements CommandExecutor {

    private PromotionEssentials plugin;

    public TokenCommand(PromotionEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("token")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to redeem/create a token!");
            } else {
                Player player = (Player) sender;

                if (!(plugin.getPluginConfig().getBoolean("token.enabled"))) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("FunctionDisabled")));
                    return true;
                }

                if (args.length == 1) {
                    // Redeem token
                    String token = args[0];

                    if (player.hasPermission("pe.token.use") || player.hasPermission("pe.token.*")) {
                        if (plugin.getTokens().contains(token)) {
                            if (plugin.getTokens().contains(token + ".expire")) {
                                // Check if expired
                                LocalDateTime dateTimeExpired = LocalDateTime.parse(plugin.getTokens().getString(token + ".expire"), plugin.getDateTimeHandler().getFormatter());

                                if (dateTimeExpired.isBefore(plugin.getDateTimeHandler().getDateTime())) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("TokenExpired")));
                                    plugin.getTokens().removeKey(token);
                                } else {
                                    plugin.getPromotionHandler().promotePlayer(player, plugin.getTokens().getString(token + ".group"));
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("TokenUse")).replace("%group%", plugin.getTokens().getString(token + ".group")));
                                    plugin.getTokens().removeKey(token);
                                }
                            } else {
                                plugin.getPromotionHandler().promotePlayer(player, plugin.getTokens().getString(token + ".group"));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("TokenUse")).replace("%group%", plugin.getTokens().getString(token + ".group")));
                                plugin.getTokens().removeKey(token);
                            }
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("TokenDoesntExist")));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("NoPermissions")));
                    }
                } else if (args.length > 1) {
                    String subcommand = args[0];

                    if (subcommand.equalsIgnoreCase("create")) {
                        if (player.hasPermission("pe.token.create") || player.hasPermission("pe.token.*")) {
                            String group = args[1];
                            String expiration = null;

                            if (args.length > 2) {
                                expiration = args[2].toLowerCase();
                            }

                            // Create token
                            UUID token = UUID.randomUUID();
                            String tokenFormatted = token.toString().replace("-", "");

                            plugin.getTokens().set(tokenFormatted + ".group", group);

                            if (!(expiration == null)) {
                                LocalDateTime dateTimeExpired = getDateTimeExpired(plugin.getDateTimeHandler().getFormatter(), expiration);

                                if (dateTimeExpired != null) {
                                    plugin.getTokens().set(tokenFormatted + ".expire", dateTimeExpired.toString());
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("CreateToken")).replace("%token%", tokenFormatted).replace("%group%", group));
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("InvalidArgs")));
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("NoPermissions")));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("InvalidArgs")));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("InvalidArgs")));
                }
            }
        }
        return true;
    }

    private LocalDateTime getDateTimeExpired(DateTimeFormatter formatter, String expiration) {
        long hours;
        long minutes;
        long seconds;

        try {
            hours = Long.parseLong(expiration.substring(0, 2));
            minutes = Long.parseLong(expiration.substring(3, 5));
            seconds = Long.parseLong(expiration.substring(6, 8));
        } catch (NumberFormatException e) {
            return null;
        }

        LocalDateTime dateTimeNow = plugin.getDateTimeHandler().getDateTime();
        LocalDateTime dateTimeExpired = dateTimeNow.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        String dateTimeExpiredStr = dateTimeExpired.format(formatter);

        return LocalDateTime.parse(dateTimeExpiredStr, plugin.getDateTimeHandler().getFormatter());
    }
}
