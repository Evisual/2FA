package me.evisual.authenticator;

import me.evisual.authenticator.security.*;
import me.evisual.authenticator.util.ImageDecoder;
import me.evisual.authenticator.util.MapItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Authenticator extends JavaPlugin implements Listener {

    // Map each player to their 2FA secret
    private final ConcurrentHashMap<UUID, String> secrets = new ConcurrentHashMap<>();
    private final String pluginName = "Authenticator"; // TODO: Update to be configurable

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            SecretGenerator secretGenerator = new SecretGenerator();
            String secret = secretGenerator.generateBase32Secret();

            // Store it so we can verify later
            secrets.put(e.getPlayer().getUniqueId(), secret);

            OtpAuthUriBuilder otpAuthUri = new OtpAuthUriBuilder(pluginName);
            QrCodeDataUriGenerator qrCodeGenerator = new QrCodeDataUriGenerator();

            String authUri = otpAuthUri.buildOtpAuthUri(secret, e.getPlayer().getName());
            String qrCode  = qrCodeGenerator.generateQrCodeDataUri(authUri, 64);

            MapItemFactory mapFactory = new MapItemFactory();
            ImageDecoder decoder = new ImageDecoder();

            ItemStack map = mapFactory.createMapItem(e.getPlayer().getWorld(), decoder.decodeBase64Png(qrCode));
            e.getPlayer().getInventory().addItem(map);

            e.getPlayer().sendMessage(ChatColor.GREEN + "2FA map given! Scan the QR code to your Authenticator.");
        } catch (NoSuchAlgorithmException ex) {
            getLogger().severe("Failed to generate QR map: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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

        TotpVerifier verifier = new TotpVerifier();
        boolean valid = verifier.verifyCode(secret, e.getMessage().trim());
        if (valid) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "✅ Code valid!");
            // you could remove the secret or mark the player as authenticated here
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "❌ Code invalid.");
        }
    }
}
