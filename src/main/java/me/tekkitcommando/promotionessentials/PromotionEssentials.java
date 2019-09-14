package me.tekkitcommando.promotionessentials;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.tekkitcommando.promotionessentials.command.ApplyCommand;
import me.tekkitcommando.promotionessentials.command.RankCommand;
import me.tekkitcommando.promotionessentials.command.TokenCommand;
import me.tekkitcommando.promotionessentials.handler.DateTimeHandler;
import me.tekkitcommando.promotionessentials.handler.PermissionsHandler;
import me.tekkitcommando.promotionessentials.handler.PromotionHandler;
import me.tekkitcommando.promotionessentials.handler.TimePromoteHandler;
import me.tekkitcommando.promotionessentials.listener.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PromotionEssentials extends JavaPlugin {

    private Logger logger;
    private Yaml config = new Yaml("config", getDataFolder().getAbsolutePath());
    private Yaml messages = new Yaml("messages", getDataFolder().getAbsolutePath());
    private Json tokens = new Json("token", getDataFolder().getAbsolutePath());
    private Json times = new Json("times", getDataFolder().getAbsolutePath());
    private Json kills = new Json("kills", getDataFolder().getAbsolutePath());
    private PermissionsHandler permissionsHandler = new PermissionsHandler(this);
    private DateTimeHandler dateTimeHandler = new DateTimeHandler();
    private TimePromoteHandler timePromoteHandler = new TimePromoteHandler(this);
    private PromotionHandler promotionHandler = new PromotionHandler(this);
    private Metrics metrics;

    // Vault
    private Economy economy = null;
    private Permission permission = null;

    @Override
    public void onDisable() {
        if (config.getBoolean("time.enabled")) {
            LocalDateTime dateTimeNow = dateTimeHandler.getDateTime();

            for (Player player : getServer().getOnlinePlayers()) {
                times.set(player.getUniqueId().toString() + ".lastLogoff", dateTimeNow.toString());
                times.set(player.getUniqueId().toString() + ".totalTime", timePromoteHandler.getTotalTime().get(player));
            }
        }

        logger.info("Disabled!");
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();

        if (!(registerEconomy())) {
            logger.warning("You must have vault installed for this plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (!(registerPermission())) {
            logger.warning("You must have vault installed for this plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        setupConfigFiles();
        setupCommands();
        setupListeners();

        if (permissionsHandler.getPermissionSystem() == null) {
            logger.warning("No permissions system found. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            logger.info("Using " + permissionsHandler.getPermissionSystem() + " for promotions.");
        }

        if (config.getBoolean("time.enabled")) {
            timePromoteHandler.startTimePromote();
        }

        if (config.getBoolean("metrics.enabled")) {
            metrics = new Metrics(this);
        }

        logger.info("Enabled!");
    }

    public Logger getPluginLogger() {
        return logger;
    }

    public Yaml getPluginConfig() {
        return config;
    }

    public Yaml getMessages() {
        return messages;
    }

    public Json getTokens() {
        return tokens;
    }

    public Json getTimes() {
        return times;
    }

    public Json getKills() {
        return kills;
    }

    public Permission getPermission() {
        return permission;
    }

    public Economy getEconomy() {
        return economy;
    }

    public DateTimeHandler getDateTimeHandler() {
        return dateTimeHandler;
    }

    public TimePromoteHandler getTimePromoteHandler() {
        return timePromoteHandler;
    }

    public PromotionHandler getPromotionHandler() {
        return promotionHandler;
    }

    private boolean registerEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return economy != null;
    }

    private boolean registerPermission() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        return permission != null;
    }

    private void setupCommands() {
        getCommand("apply").setExecutor(new ApplyCommand(this));
        getCommand("token").setExecutor(new TokenCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this),this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
    }

    private void setupConfigFiles() {
        Map<String, String> timedRanks = new HashMap<>();
        Map<String, Double> purchasedRanks = new HashMap<>();
        List<String> noPromote = new ArrayList<>();
        List<String> consoleCommands = new ArrayList<>();
        List<String> playerCommands = new ArrayList<>();

        timedRanks.put("member", "00h15m30s");
        timedRanks.put("elite", "24h00m00s");
        timedRanks.put("legend", "48h30m15s");

        purchasedRanks.put("member", 1000.00);
        purchasedRanks.put("elite", 10000.00);
        purchasedRanks.put("legend", 100000.00);

        noPromote.add("Admin");
        noPromote.add("Mod");
        noPromote.add("Helper");
        noPromote.add("Owner");

        consoleCommands.add("eco give %player% 1000");
        playerCommands.add("me got promoted to %group%");

        config.setDefault("metrics.enabled", true);

        config.setDefault("token.enabled", true);

        config.setDefault("apply.enabled", false);
        config.setDefault("apply.password", "changeme");
        config.setDefault("apply.default", "default");
        config.setDefault("apply.promotion", "member");
        config.setDefault("apply.freeze", false);
        config.setDefault("apply.mute", false);
        config.setDefault("apply.kickWrongPW", false);
        config.setDefault("apply.blockViewChat", false);

        config.setDefault("time.enabled", false);
        config.setDefault("time.groups", timedRanks);
        config.setDefault("time.noPromote", noPromote);
        config.setDefault("time.countOffline", false);

        config.setDefault("buy.enabled", true);
        config.setDefault("buy.rankListHeader", "&aPurchasable Ranks");
        config.setDefault("buy.rankListColor", "RED");
        config.setDefault("buy.groups", purchasedRanks);

        config.setDefault("kill.enabled", false);
        config.setDefault("kill.countFriendlyMobs", false);
        config.setDefault("kill.member.players", 10);
        config.setDefault("kill.member.mobs", 10);
        config.setDefault("kill.elite.players", 100);
        config.setDefault("kill.elite.mobs", 100);
        config.setDefault("kill.legend.players", 1000);
        config.setDefault("kill.legend.mobs", 1000);

        config.setDefault("commands.member.console", consoleCommands);
        config.setDefault("commands.member.player", playerCommands);

        messages.setDefault("NoPermissions", "&c[PromotionEssentials] You do not have permission to do this!");
        messages.setDefault("CreatedSign", "&a[PromotionEssentials] Successfully created a promotion sign!");
        messages.setDefault("UsedSign", "&a[PromotionEssentials] Successfully promoted to %group%!");
        messages.setDefault("UsedPW", "&a[PromotionEssentials] You have been successfully promoted to %group%!");
        messages.setDefault("WrongPW", "&c[PromotionEssentials] Wrong PW!");
        messages.setDefault("TokenUse", "&a[PromotionEssentials] You have been successfully promoted to %group%!");
        messages.setDefault("CreateToken", "&a[PromotionEssentials] Created token %token% for %group%!");
        messages.setDefault("TokenExpired", "&c[PromotionEssentials] That token has expired!");
        messages.setDefault("TokenDoesntExist", "&c[PromotionEssentials] That token doesn't exist!");
        messages.setDefault("Join", "&a[PromotionEssentials] %player%, &aplease write /apply [Password] to get Permissions to build!");
        messages.setDefault("Mute", "&c[PromotionEssentials] You are not allowed to chat!");
        messages.setDefault("FunctionDisabled", "&c[PromotionEssentials] This function has been disabled by the server administrator!");
        messages.setDefault("CantBuyRank", "&c[PromotionEssentials] You can not buy this rank! Either it doesn't exist or you already own it.");
        messages.setDefault("NoMoney", "&c[PromotionEssentials] You do not have enough money to buy this rank!");
        messages.setDefault("BoughtRank", "&a[PromotionEssentials] Bought rank %group%!");
        messages.setDefault("PromotedAfterTime", "&a[PromotionEssentials] You have been promoted to %group%!");
        messages.setDefault("InvalidArgs", "&c[PromotionEssentials] Invalid arguments!");
        messages.setDefault("PromotedAfterKills", "&a[PromotionEssentials] You have been promoted to %group% for your kills!");
    }
}
