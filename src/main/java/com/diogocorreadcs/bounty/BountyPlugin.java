package com.diogocorreadcs.bounty;

import com.diogocorreadcs.bounty.commands.BountyCommand;
import com.diogocorreadcs.bounty.commands.BountyTabCompleter;
import com.diogocorreadcs.bounty.database.BountyRepository;
import com.diogocorreadcs.bounty.database.DatabaseManager;
import com.diogocorreadcs.bounty.database.HunterStatsRepository;
import com.diogocorreadcs.bounty.integrations.ClanSystemHook;
import com.diogocorreadcs.bounty.integrations.DiscordWebhookHook;
import com.diogocorreadcs.bounty.integrations.PlaceholderAPIHook;
import com.diogocorreadcs.bounty.integrations.PlayerPointsHook;
import com.diogocorreadcs.bounty.integrations.VaultHook;
import com.diogocorreadcs.bounty.integrations.WorldGuardHook;
import com.diogocorreadcs.bounty.listeners.BountyDeathListener;
import com.diogocorreadcs.bounty.listeners.CombatTagListener;
import com.diogocorreadcs.bounty.listeners.MenuClickListener;
import com.diogocorreadcs.bounty.listeners.PlayerConnectionListener;
import com.diogocorreadcs.bounty.managers.BountyManager;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.diogocorreadcs.bounty.managers.MessagesManager;
import com.diogocorreadcs.bounty.menus.TopHuntersMenu;
import com.diogocorreadcs.bounty.menus.WantedListMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BountyPlugin extends JavaPlugin {

    private static BountyPlugin instance;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private DatabaseManager databaseManager;
    private BountyRepository bountyRepository;
    private HunterStatsRepository hunterStatsRepository;
    private BountyManager bountyManager;

    private WantedListMenu wantedListMenu;
    private TopHuntersMenu topHuntersMenu;

    private VaultHook vaultHook;
    private ClanSystemHook clanSystemHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private WorldGuardHook worldGuardHook;
    private PlayerPointsHook playerPointsHook;
    private DiscordWebhookHook discordWebhookHook;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.messagesManager = new MessagesManager(this);
        this.messagesManager.loadMessages();

        this.vaultHook = new VaultHook(this);
        if (!this.vaultHook.setupEconomy()) {
            getLogger().severe("Vault ou plugin de economia nao encontrado! Desabilitando o dBounty...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.databaseManager = new DatabaseManager(this, this.configManager);
        if (!this.databaseManager.connect()) {
            getLogger().severe("Falha ao conectar no banco de dados! Desabilitando o dBounty...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.bountyRepository = new BountyRepository(this, this.databaseManager);
        this.bountyRepository.createTables();

        this.hunterStatsRepository = new HunterStatsRepository(this, this.databaseManager);
        this.hunterStatsRepository.createTable();

        setupIntegrations();

        this.bountyManager = new BountyManager(this, this.bountyRepository, this.hunterStatsRepository, this.configManager);
        this.bountyManager.loadCache();

        this.wantedListMenu = new WantedListMenu(this);
        this.topHuntersMenu = new TopHuntersMenu(this);

        registerCommands();
        registerListeners();

        getLogger().info("dBounty v" + getDescription().getVersion() + " habilitado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (this.bountyManager != null) {
            this.bountyManager.saveAllSync();
        }
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
        getLogger().info("dBounty desabilitado.");
    }

    private void setupIntegrations() {
        PluginManager pm = Bukkit.getPluginManager();

        this.discordWebhookHook = new DiscordWebhookHook(this);
        if (this.discordWebhookHook.isEnabled()) {
            getLogger().info("Integracao com Discord Webhook ativada!");
        }

        if (isPluginActive(pm, "dClanSystem")) {
            this.clanSystemHook = new ClanSystemHook(this);
            getLogger().info("Integracao com dClanSystem ativada!");
        }

        if (isPluginActive(pm, "WorldGuard")) {
            this.worldGuardHook = new WorldGuardHook(this);
            getLogger().info("Integracao com WorldGuard ativada!");
        }

        if (isPluginActive(pm, "PlayerPoints")) {
            this.playerPointsHook = new PlayerPointsHook(this);
            getLogger().info("Integracao com PlayerPoints ativada!");
        }

        if (isPluginActive(pm, "PlaceholderAPI")) {
            this.placeholderAPIHook = new PlaceholderAPIHook(this);
            this.placeholderAPIHook.register();
            getLogger().info("Integracao com PlaceholderAPI ativada!");
        }
    }

    private boolean isPluginActive(PluginManager pm, String name) {
        return pm.getPlugin(name) != null && pm.isPluginEnabled(name);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("bounty");
        if (command != null) {
            command.setExecutor(new BountyCommand(this));
            command.setTabCompleter(new BountyTabCompleter());
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BountyDeathListener(this), this);
        pm.registerEvents(new CombatTagListener(this), this);
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new MenuClickListener(this), this);
    }

    public static BountyPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BountyRepository getBountyRepository() {
        return bountyRepository;
    }

    public HunterStatsRepository getHunterStatsRepository() {
        return hunterStatsRepository;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public WantedListMenu getWantedListMenu() {
        return wantedListMenu;
    }

    public TopHuntersMenu getTopHuntersMenu() {
        return topHuntersMenu;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public ClanSystemHook getClanSystemHook() {
        return clanSystemHook;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public PlayerPointsHook getPlayerPointsHook() {
        return playerPointsHook;
    }

    public DiscordWebhookHook getDiscordWebhookHook() {
        return discordWebhookHook;
    }

    public boolean hasClanSystem() {
        return clanSystemHook != null && clanSystemHook.isAvailable();
    }
}