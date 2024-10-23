package nl.de3d.visualpvp.visualkits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private Map<UUID, Map<String, Long>> categoryCooldowns = new HashMap<>();
    private KitPlugin plugin;

    public CooldownManager(KitPlugin plugin) {
        this.plugin = plugin;
    }

    public long parseCooldown(String cooldownString) {
        if (cooldownString == null || cooldownString.isEmpty()) {
            return 0;
        }

        long timeInMillis = 0;
        String[] parts = cooldownString.split("(?<=\\d)(?=\\D)");
        for (String part : parts) {
            try {
                String unit = part.substring(part.length() - 1);
                String number = part.substring(0, part.length() - 1);
                if (number.isEmpty()) {
                    number = "1";
                    plugin.getLogger().warning("No cooldown found '" + part + "', standard to 1.");
                }
                int value = Integer.parseInt(number);

                switch (unit) {
                    case "d":
                        timeInMillis += TimeUnit.DAYS.toMillis(value);
                        break;
                    case "h":
                        timeInMillis += TimeUnit.HOURS.toMillis(value);
                        break;
                    case "m":
                        timeInMillis += TimeUnit.MINUTES.toMillis(value);
                        break;
                    case "s":
                        timeInMillis += TimeUnit.SECONDS.toMillis(value);
                        break;
                    default:
                        plugin.getLogger().warning("Invalid time '" + unit + "' in cooldown-string.");
                        break;
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid time in part '" + part + "'.");
            }
        }
        return timeInMillis;
    }

    // Controleer of de categorie op cooldown is voor de speler
    public boolean isCategoryOnCooldown(UUID playerUUID, String categoryName) {
        return categoryCooldowns.containsKey(playerUUID)
                && categoryCooldowns.get(playerUUID).containsKey(categoryName)
                && categoryCooldowns.get(playerUUID).get(categoryName) > System.currentTimeMillis();
    }

    // Stel de cooldown in voor een categorie voor een speler
    public void setCategoryCooldown(UUID playerUUID, String categoryName, long cooldownDuration) {
        categoryCooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(categoryName, System.currentTimeMillis() + cooldownDuration);
    }

    // Haal de resterende cooldown op voor een categorie
    public long getRemainingCategoryCooldown(UUID playerUUID, String categoryName) {
        if (isCategoryOnCooldown(playerUUID, categoryName)) {
            return categoryCooldowns.get(playerUUID).get(categoryName) - System.currentTimeMillis();
        }
        return 0;
    }

    public String formatDuration(long duration) {
        long seconds = duration / 1000 % 60;
        long minutes = duration / (1000 * 60) % 60;
        long hours = duration / (1000 * 60 * 60) % 24;
        long days = duration / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
