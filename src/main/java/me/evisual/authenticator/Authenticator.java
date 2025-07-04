package me.evisual.authenticator;

import me.evisual.authenticator.security.TwoFactorAuthUtil;
import me.evisual.authenticator.util.MapDrawUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Authenticator extends JavaPlugin implements Listener {

    // Map each player to their 2FA secret
    private final ConcurrentHashMap<UUID, String> secrets = new ConcurrentHashMap<>();

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            TwoFactorAuthUtil util = new TwoFactorAuthUtil("TestPlugin");
            String secret = util.generateSecret();
            // Store it so we can verify later
            secrets.put(e.getPlayer().getUniqueId(), secret);

            String authUri = util.buildOtpAuthUri(secret, e.getPlayer().getName());
            String qrCode  = util.generateQrCodeDataUri(authUri, 64);

            MapDrawUtils.giveDrawnMap(e.getPlayer(), qrCode);
            e.getPlayer().sendMessage(ChatColor.GREEN + "2FA map given! Scan the QR code to your Authenticator.");
        } catch (NoSuchAlgorithmException ex) {
            getLogger().severe("Failed to generate QR map: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        String secret = secrets.get(uuid);
        if (secret == null) {
            e.getPlayer().sendMessage(ChatColor.RED + "You haven't generated a 2FA QR yet. Rejoin to get one.");
            return;
        }

        TwoFactorAuthUtil util = new TwoFactorAuthUtil("TestPlugin");
        boolean valid = util.verifyCode(secret, e.getMessage().trim());
        if (valid) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "✅ Code valid!");
            // you could remove the secret or mark the player as authenticated here
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "❌ Code invalid.");
        }
    }
}
