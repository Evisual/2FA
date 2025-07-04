package me.evisual.authenticator.util.commands;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Command
{
    protected @Getter String command;
    protected @Nullable @Getter String permission;

    public Command(@NotNull String command, @Nullable String permission)
    {
        this.command = command;
        this.permission = permission;
    }

    public abstract void noPermissionEvent(@NotNull CommandSender sender);
    public abstract boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args);
    public abstract List<String> tabOptions(@NotNull CommandSender sender, @NotNull String[] args);

    public abstract @NotNull String getUsage();
    public abstract @NotNull String getDescription();
    public abstract @NotNull String[] getAliases();

    public boolean hasPermission(CommandSender sender)
    {
        // No permission -- everyone has permission
        if(permission == null)
            return true;

        return sender.hasPermission(permission);
    }

    public final void execute(CommandSender sender, @NotNull String[] args)
    {
        if(hasPermission(sender))
            runCommand(sender, args);
        else
            noPermissionEvent(sender);
    }
}
