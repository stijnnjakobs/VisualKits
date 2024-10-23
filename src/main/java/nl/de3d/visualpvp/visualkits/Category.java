package nl.de3d.visualpvp.visualkits;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private ItemStack item;
    private String description;
    private boolean glow;
    private String cooldown;
    private int slot; // Toegevoegd voor slot
    private List<Kit> kits = new ArrayList<>();

    public Category(String name, ItemStack item, String description, boolean glow, String categoryCooldown, int slot) {
        this.name = name;
        this.item = item;
        this.description = description;
        this.glow = glow;
        this.cooldown = categoryCooldown;
        this.slot = slot;
    }

    public void addKit(Kit kit) {
        kits.add(kit);
    }

    // Getters
    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public List<Kit> getKits() {
        return kits;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGlow() {
        return glow;
    }

    public String getCooldown() {
        return cooldown;
    }

    public int getSlot() {
        return slot;
    }
}
