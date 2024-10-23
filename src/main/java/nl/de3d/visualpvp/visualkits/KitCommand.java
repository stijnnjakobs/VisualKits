package nl.de3d.visualpvp.visualkits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private KitPlugin plugin;

    public KitCommand(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.translate("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open de categorie-GUI
            plugin.getInventoryClickListener().openCategoriesGUI(player);
            return true;
        }

        // Als er een argument is, probeer de kit direct toe te passen
        String kitName = args[0];
        KitManager kitManager = plugin.getKitManager();
        Kit kit = kitManager.getKitByName(kitName);

        if (kit == null) {
            String message = plugin.getConfig().getString("messages.kit_not_found");
            player.sendMessage(ChatUtil.translate(message));
            return true;
        }

        // Vind de categorie van de kit
        Category category = kitManager.getCategoryOfKit(kitName);
        if (category == null) {
            String message = plugin.getConfig().getString("messages.category_not_found");
            player.sendMessage(ChatUtil.translate(message));
            return true;
        }

        // Controleer categorie-cooldown
        CooldownManager cooldownManager = plugin.getCooldownManager();
        if (cooldownManager.isCategoryOnCooldown(player.getUniqueId(), category.getName())) {
            long remaining = cooldownManager.getRemainingCategoryCooldown(player.getUniqueId(), category.getName());
            String formattedTime = cooldownManager.formatDuration(remaining);
            String message = plugin.getConfig().getString("messages.category_on_cooldown")
                    .replace("{time}", formattedTime);
            player.sendMessage(ChatUtil.translate(message));
            return true;
        }

        // Controleer permissies
        boolean hasPermission = true;
        if (kit.getPermissions() != null && !kit.getPermissions().isEmpty()) {
            hasPermission = false;
            for (String perm : kit.getPermissions()) {
                if (player.hasPermission(perm)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            String message = plugin.getConfig().getString("messages.no_permission");
            player.sendMessage(ChatUtil.translate(message));
            return true;
        }

        // Geef items aan de speler
        kitManager.applyKit(player, kitName);

        // Stel categorie-cooldown in
        long cooldownDuration;
        if (category.getCooldown() != null && !category.getCooldown().isEmpty()) {
            cooldownDuration = cooldownManager.parseCooldown(category.getCooldown());
        } else {
            // Als er geen categorie-cooldown is, gebruik dan de cooldown van de kit
            cooldownDuration = cooldownManager.parseCooldown(kit.getCooldown());
        }
        cooldownManager.setCategoryCooldown(player.getUniqueId(), category.getName(), cooldownDuration);

        // Stuur een succesvol bericht
        String successMessage = plugin.getConfig().getString("messages.kit_claimed")
                .replace("{kit}", kit.getName())
                .replace("{category}", category.getName());
        player.sendMessage(ChatUtil.translate(successMessage));

        return true;
    }
}
