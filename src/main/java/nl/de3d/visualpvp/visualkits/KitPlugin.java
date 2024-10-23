package nl.de3d.visualpvp.visualkits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class KitPlugin extends JavaPlugin {
    private KitManager kitManager;
    private CooldownManager cooldownManager;
    private InventoryClickListener inventoryClickListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        logOnEnable();

        kitManager = new KitManager(this);
        cooldownManager = new CooldownManager(this);
        inventoryClickListener = new InventoryClickListener(this);

        KitCommand kitCommand = new KitCommand(this);
        getCommand("kit").setExecutor(kitCommand);
        getCommand("kits").setExecutor(kitCommand); // Als 'kits' hetzelfde commando moet zijn

        getServer().getPluginManager().registerEvents(inventoryClickListener, this);
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public InventoryClickListener getInventoryClickListener() {
        return inventoryClickListener;
    }

    public void logOnEnable() {
        Bukkit.getLogger().info(ChatColor.DARK_GRAY + "=======================================");
        Bukkit.getLogger().info(ChatColor.AQUA + "      Plugin: " + ChatColor.GREEN + getDescription().getName());
        Bukkit.getLogger().info(ChatColor.AQUA + "      Version: " + ChatColor.GREEN + getDescription().getVersion());
        Bukkit.getLogger().info(ChatColor.AQUA + "      Author: " + ChatColor.GREEN + getDescription().getAuthors());
        Bukkit.getLogger().info(ChatColor.AQUA + "      Plugin made for: " + ChatColor.GREEN + "VisualPvP");
        Bukkit.getLogger().info(ChatColor.DARK_GRAY + "=======================================");
        Bukkit.getLogger().info(ChatColor.GOLD + "  " + ChatColor.BOLD + "VisualPvP plugin has been successfully started!");
        Bukkit.getLogger().info(ChatColor.DARK_GRAY + "=======================================");
    }

}
