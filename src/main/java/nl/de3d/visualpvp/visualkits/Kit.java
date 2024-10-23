package nl.de3d.visualpvp.visualkits;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Kit {
    private String name;
    private List<ItemStack> items;
    private String description; // Add description field
    private ItemStack item; // Add item field
    private String cooldown;
    private List<String> permissions;
    private boolean glow;
    private int slot; // Toegevoegd voor slot

    public Kit(String name, List<ItemStack> items, ItemStack item, String cooldown, String description, List<String> permissions, boolean glowKit, int slot) {
        this.name = name;
        this.items = items;
        this.item = item;
        this.description = description;
        this.cooldown = cooldown;
        this.permissions = permissions;
        this.glow = glowKit;
        this.slot = slot;
    }

    // Getters
    public String getName() {
        return name;
    }
    public List<ItemStack> getItems() {
        return items;
    }
    public String getCooldown() {
        return cooldown;
    }
    public List<String> getPermissions() {
        return permissions;
    }
    public ItemStack getItem() {
        return item;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGlow() {
        return glow;
    }

    public int getSlot() {
        return slot;
    }
}
