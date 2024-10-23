package nl.de3d.visualpvp.visualkits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public class KitManager {
    private KitPlugin plugin;
    private Map<String, Category> categories = new HashMap<>();

    public KitManager(KitPlugin plugin) {
        this.plugin = plugin;
        loadKitsFromConfig();
    }

    public void loadKitsFromConfig() {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String categoryKey : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
                String name = categorySection.getString("name");
                Material itemMaterial = Material.valueOf(categorySection.getString("item").toUpperCase());
                ItemStack item = new ItemStack(itemMaterial);
                String description = categorySection.getString("description");
                boolean glow = categorySection.getBoolean("settings.glow", false);
                String categoryCooldown = categorySection.getString("settings.cooldown", "");
                int categorySlot = categorySection.getInt("slot", -1); // -1 betekent geen slot ingesteld

                // Waarschuwing bij ongeldig slot
                if (categorySlot != -1 && (categorySlot < 0 || categorySlot > 53)) {
                    plugin.getLogger().warning("Categorie '" + name + "' heeft een ongeldig slot ingesteld: " + categorySlot + ". Slot wordt genegeerd.");
                    categorySlot = -1;
                }

                Category category = new Category(name, item, description, glow, categoryCooldown, categorySlot);

                ConfigurationSection kitsSection = categorySection.getConfigurationSection("kits");
                if (kitsSection != null) {
                    for (String kitKey : kitsSection.getKeys(false)) {
                        ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);

                        if (kitSection == null) {
                            plugin.getLogger().warning("Kit section '" + kitKey + "' is null in category '" + name + "'");
                            continue;
                        }

                        String kitName = kitSection.getString("name", kitKey);

                        // Veilig materiaal ophalen
                        String materialString = kitSection.getString("item");
                        if (materialString == null) {
                            plugin.getLogger().warning("Material voor kit '" + kitName + "' in categorie '" + name + "' ontbreekt.");
                            continue;
                        }

                        Material kitMaterial;
                        try {
                            kitMaterial = Material.valueOf(materialString.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Ongeldig materiaal '" + materialString + "' voor kit '" + kitName + "' in categorie '" + name + "'");
                            continue;
                        }

                        ItemStack kitItemStack = new ItemStack(kitMaterial);
                        String kitDescription = kitSection.getString("description", kitName);
                        String cooldown = kitSection.getString("settings.cooldown", "0s");
                        boolean kitGlow = kitSection.getBoolean("settings.glow", false);
                        List<String> permissions = kitSection.getStringList("permission");
                        List<ItemStack> items = new ArrayList<>();
                        int kitSlot = kitSection.getInt("slot", -1); // -1 betekent geen slot ingesteld

                        // Waarschuwing bij ongeldig slot
                        if (kitSlot != -1 && (kitSlot < 0 || kitSlot > 53)) {
                            plugin.getLogger().warning("Kit '" + kitName + "' in categorie '" + name + "' heeft een ongeldig slot ingesteld: " + kitSlot + ". Slot wordt genegeerd.");
                            kitSlot = -1;
                        }

                        // Lees items als lijst van maps
                        List<Map<?, ?>> itemMaps = kitSection.getMapList("items");
                        for (Map<?, ?> itemMap : itemMaps) {
                            String itemString = (String) itemMap.get("item");
                            if (itemString == null) {
                                plugin.getLogger().warning("Item is missing in kit '" + kitName + "' in category '" + name + "'.");
                                continue;
                            }

                            String itemName = (String) itemMap.get("name");
                            List<String> itemLore = (List<String>) itemMap.get("lore");

                            ItemStack kitItem = parseItemString(itemString);
                            if (kitItem != null) {
                                ItemMeta meta = kitItem.getItemMeta();

                                // Stel aangepaste naam in indien aanwezig
                                if (itemName != null && !itemName.isEmpty()) {
                                    meta.setDisplayName(ChatUtil.translate(itemName));
                                }

                                // Stel lore in indien aanwezig
                                if (itemLore != null && !itemLore.isEmpty()) {
                                    List<String> translatedLore = new ArrayList<>();
                                    translatedLore.add(""); // Voeg een lege regel toe
                                    for (String loreLine : itemLore) {
                                        translatedLore.add(ChatUtil.translate(loreLine));
                                    }
                                    meta.setLore(translatedLore);
                                }

                                kitItem.setItemMeta(meta);
                                items.add(kitItem);
                            }
                        }

                        Kit kit = new Kit(kitName, items, kitItemStack, cooldown, kitDescription, permissions, kitGlow, kitSlot);
                        category.addKit(kit);
                    }
                }

                categories.put(name, category);
            }
        }
    }

    private ItemStack parseItemString(String itemString) {
        try {
            plugin.getLogger().info("Parsing item string: " + itemString);

            String[] parts = itemString.split(":", 2);
            Material material = Material.valueOf(parts[0].toUpperCase());
            plugin.getLogger().info("Material: " + material.name());

            ItemStack item;

            // Controleer of het item een potion is
            if (material == Material.POTION) {
                plugin.getLogger().info("Detected a potion item.");

                item = new ItemStack(material);

                if (parts.length > 1) {
                    String[] effectParts = parts[1].split(",");
                    plugin.getLogger().info("Potion effect parts: " + Arrays.toString(effectParts));

                    if (effectParts.length == 3) {
                        String effectName = effectParts[0].toUpperCase();
                        int duration = Integer.parseInt(effectParts[1]); // De duur wordt genegeerd in Spigot 1.8.8, maar blijft hier voor later gebruik
                        int amplifier = Integer.parseInt(effectParts[2]);

                        plugin.getLogger().info("Effect name: " + effectName + ", Duration: " + duration + ", Amplifier: " + amplifier);

                        // Gebruik de potion ID's om de juiste durability in te stellen
                        short durability = getPotionDurability(effectName, amplifier);

                        if (durability != -1) {
                            plugin.getLogger().info("Setting durability for potion: " + durability);
                            item.setDurability(durability);
                        } else {
                            plugin.getLogger().warning("Unknown potion type: " + effectName);
                        }
                    } else {
                        plugin.getLogger().warning("Invalid potion effect format in item string: " + itemString);
                    }
                }
            } else {
                plugin.getLogger().info("Detected a non-potion item.");

                // Normale item verwerking met enchantments
                item = new ItemStack(material);
                if (parts.length > 1) {
                    ItemMeta meta = item.getItemMeta();
                    String[] enchantments = parts[1].split(",");
                    plugin.getLogger().info("Enchantments: " + Arrays.toString(enchantments));

                    for (String enchantString : enchantments) {
                        String[] enchantParts = enchantString.split(":");
                        if (enchantParts.length != 2) {
                            plugin.getLogger().warning("Invalid enchantment specification: " + enchantString);
                            continue;
                        }
                        Enchantment enchantment = Enchantment.getByName(enchantParts[0].toUpperCase());
                        int level = Integer.parseInt(enchantParts[1]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                            plugin.getLogger().info("Added enchantment: " + enchantment.getName() + " level " + level);
                        } else {
                            plugin.getLogger().warning("Unknown enchantment: " + enchantParts[0]);
                        }
                    }
                    item.setItemMeta(meta);
                }
            }

            plugin.getLogger().info("Item successfully parsed: " + item);
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not parse item string: " + itemString);
            e.printStackTrace();
            return null;
        }
    }

    // Deze functie retourneert de juiste potion durability op basis van het effect en het amplifier niveau
    private short getPotionDurability(String effectName, int amplifier) {
        switch (effectName) {
            case "INSTANT_HEAL":
                return (short) (amplifier == 1 ? 8229 : 8261); // Instant Health II of I
            case "SPEED":
                return (short) (amplifier == 1 ? 8226 : 8194); // Speed II of I
            case "STRENGTH":
                return (short) (amplifier == 1 ? 8233 : 8201); // Strength II of I
            // Voeg hier meer potion effecten toe gebaseerd op je lijst
            default:
                return -1; // Onbekend potion type
        }
    }


    public Category getCategory(String name) {
        return categories.get(name);
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public Kit getKitByName(String kitName) {
        for (Category category : categories.values()) {
            for (Kit kit : category.getKits()) {
                if (kit.getName().equalsIgnoreCase(kitName)) {
                    return kit;
                }
            }
        }
        return null;
    }

    public Category getCategoryOfKit(String kitName) {
        for (Category category : categories.values()) {
            for (Kit kit : category.getKits()) {
                if (kit.getName().equalsIgnoreCase(kitName)) {
                    return category;
                }
            }
        }
        return null;
    }

    public void applyKit(Player player, String kitName) {
        Kit kit = getKitByName(kitName);
        if (kit == null) {
            player.sendMessage(ChatUtil.translate(plugin.getConfig().getString("messages.kit_not_found")));
            return;
        }
        // Geef items aan de speler
        for (ItemStack item : kit.getItems()) {
            player.getInventory().addItem(item);
        }
        String message = plugin.getConfig().getString("messages.kit_applied")
                .replace("{kit}", kit.getName());
        player.sendMessage(ChatUtil.translate(message));
    }
}
