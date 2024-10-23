package nl.de3d.visualpvp.visualkits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClickListener implements Listener {

    private KitPlugin plugin;

    public InventoryClickListener(KitPlugin plugin) {
        this.plugin = plugin;
    }

    public void openCategoriesGUI(Player player) {
        int size = 27;
        String menuTitle = plugin.getConfig().getString("menu.title");
        Inventory inv = Bukkit.createInventory(null, size, ChatUtil.translate(menuTitle));

        for (Category category : plugin.getKitManager().getCategories().values()) {
            ItemStack item = category.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtil.translate(category.getName()));
            if (category.isGlow()) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            // Voeg beschrijving toe als lore
            if (category.getDescription() != null) {
                meta.setLore(java.util.Arrays.asList(ChatUtil.translate(category.getDescription())));
            }
            item.setItemMeta(meta);

            // Bepaal de slot
            int slot = category.getSlot();
            if (slot == -1) {
                // Geen slot ingesteld, gebruik de volgende beschikbare slot
                slot = inv.firstEmpty();
                if (slot == -1) {
                    // Inventaris is vol
                    plugin.getLogger().warning("Inventory is full. Can't assign '" + category.getName() + "'");
                    continue;
                }
            }

            // Controleer of de slot binnen bereik is
            if (slot < 0 || slot >= size) {
                plugin.getLogger().warning("Categorie '" + category.getName() + "' heeft een ongeldig slot ingesteld: " + slot + ". Slot wordt genegeerd.");
                slot = inv.firstEmpty();
                if (slot == -1) {
                    plugin.getLogger().warning("Inventory is full. Can't assign '" + category.getName() + "'");
                    continue;
                }
            }

            inv.setItem(slot, item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Controleer of de klik in onze aangepaste inventaris is
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String menuTitle = plugin.getConfig().getString("menu.title");

        if (event.getView().getTitle().equals(ChatUtil.translate(menuTitle))) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String categoryName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            Category category = getCategoryByName(categoryName);
            if (category != null) {
                openKitsGUI(player, category);
            }
            return;
        }

        if (event.getView().getTitle().startsWith("Kits: ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String kitName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            String categoryName = event.getView().getTitle().substring("Kits: ".length());
            Category category = getCategoryByName(categoryName);
            if (category == null) {
                String message = plugin.getConfig().getString("messages.category_not_found");
                player.sendMessage(ChatUtil.translate(message));
                return;
            }

            Kit kit = getKitByName(categoryName, kitName);
            if (kit != null) {
                // Verwerk het claimen van de kit
                handleKitClaim(player, kit, category);
            }
        }
    }

    private void openKitsGUI(Player player, Category category) {
        int size = 27;
        Inventory inv = Bukkit.createInventory(null, size, "Kits: " + category.getName());

        for (Kit kit : category.getKits()) {
            ItemStack item = kit.getItem().clone();  // Use the item directly from Kit class
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtil.translate(kit.getName()));

            if (kit.isGlow()) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Add description as lore if available
            if (kit.getDescription() != null) {
                meta.setLore(java.util.Arrays.asList(ChatUtil.translate(kit.getDescription())));
            }
            item.setItemMeta(meta);

            // Determine the slot
            int slot = kit.getSlot();
            if (slot == -1) {
                slot = inv.firstEmpty();
                if (slot == -1) {
                    plugin.getLogger().warning("Inventory is full. Can't assign kit '" + kit.getName() + "'");
                    continue;
                }
            }

            // Ensure the slot is valid
            if (slot < 0 || slot >= size) {
                plugin.getLogger().warning("Kit '" + kit.getName() + "' in category '" + category.getName() + "' has an invalid slot set: " + slot + ". Slot will be ignored.");
                slot = inv.firstEmpty();
                if (slot == -1) {
                    plugin.getLogger().warning("Inventory is full. Can't assign kit '" + kit.getName() + "'");
                    continue;
                }
            }

            inv.setItem(slot, item);
        }

        player.openInventory(inv);
    }

    private void handleKitClaim(Player player, Kit kit, Category category) {
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
            return;
        }

        // Controleer categorie-cooldown
        CooldownManager cooldownManager = plugin.getCooldownManager();
        if (cooldownManager.isCategoryOnCooldown(player.getUniqueId(), category.getName())) {
            long remaining = cooldownManager.getRemainingCategoryCooldown(player.getUniqueId(), category.getName());
            String formattedTime = cooldownManager.formatDuration(remaining);
            String message = plugin.getConfig().getString("messages.category_on_cooldown")
                    .replace("{time}", formattedTime);
            player.sendMessage(ChatUtil.translate(message));
            return;
        }

        // Pas de kit toe
        plugin.getKitManager().applyKit(player, kit.getName());

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
    }

    private Category getCategoryByName(String name) {
        return plugin.getKitManager().getCategory(name);
    }

    private Kit getKitByName(String categoryName, String kitName) {
        Category category = getCategoryByName(categoryName);
        if (category != null) {
            for (Kit kit : category.getKits()) {
                if (kit.getName().equalsIgnoreCase(kitName)) {
                    return kit;
                }
            }
        }
        return null;
    }
}
