package me.evisual.authenticator;

import lombok.Getter;
import me.evisual.authenticator.commands.TwoFactorCommand;
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

    // I hate this -- will be removed ASAP
    @Getter
    private static Authenticator instance;

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("2fa").setExecutor(new TwoFactorCommand());
        instance = this;
    }

    public String getSecret(UUID uuid)
    {
        return secrets.get(uuid);
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
}
