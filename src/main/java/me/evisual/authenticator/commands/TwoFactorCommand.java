package me.evisual.authenticator.commands;

import me.evisual.authenticator.Authenticator;
import me.evisual.authenticator.security.TotpVerifier;
import me.evisual.authenticator.util.commands.Command;
import me.evisual.authenticator.util.commands.SuperCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TwoFactorCommand extends SuperCommand
{
    private final Authenticator plugin;
    private final Command[] commands;

    public TwoFactorCommand(@NotNull String command, @Nullable String permission, Authenticator plugin)
    {
        super(command, permission);

        this.plugin = plugin;

        this.commands = new Command[] {};


    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        // TODO: In the future, this needs to be made a lot safer. There's many ways
        // to cause this method to break unintentionally as it's not very robust
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
        if(verifier.verifyCode(secret, args[0]))
            player.sendMessage(ChatColor.GREEN + "Authenticated!");
        else
            player.sendMessage(ChatColor.RED + "Wrong Code!");
        return true;
    }

    @Override
    public void subCommandNotFoundEvent(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Not found"); // TODO: Update with actual messages
    }

    @Override
    public void noPermissionEvent(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "No permission"); // TODO: Update with actual messages
    }

    @Override
    public @NotNull Command[] getSubCommands() {
        return this.commands;
    }



    @Override
    public List<String> tabOptions(@NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }

    @Override
    public @NotNull String getDescription() {
        return "";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[0];
    }
}
