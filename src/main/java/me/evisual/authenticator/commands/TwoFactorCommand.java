package me.evisual.authenticator.commands;

import me.evisual.authenticator.Authenticator;
import me.evisual.authenticator.security.TotpVerifier;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TwoFactorCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        // Sanity check -- should never happen
        if (!cmd.getName().equalsIgnoreCase("2fa")) return true; // TODO: Add aliases

        // Check for arguments (none added yet)

        if(!(sender instanceof Player player))
        {
            // In theory a check could be added to 2fa the console on reboot, for now
            // we're going to limit it to players for simplicity
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command");
            return true;
        }

        String secret = Authenticator.getInstance().getSecret(player.getUniqueId());

        if(secret == null)
        {
            player.sendMessage(ChatColor.RED + "You have not set up 2fa yet!");
            return true;
        }

        TotpVerifier verifier = new TotpVerifier();
        verifier.verifyCode(secret, args[0]);
        return true;
    }
}
