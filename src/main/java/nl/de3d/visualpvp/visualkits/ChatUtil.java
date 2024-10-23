package nl.de3d.visualpvp.visualkits;

import org.bukkit.ChatColor;

public class ChatUtil {
    /**
     * Vertaal &-kleurcodes naar Bukkit ChatColor codes.
     *
     * @param message Het bericht met &-kleurcodes.
     * @return Het vertaalde bericht met originele kleurcodes.
     */
    public static String translate(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
