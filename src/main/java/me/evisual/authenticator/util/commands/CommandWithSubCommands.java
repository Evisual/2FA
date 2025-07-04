package me.evisual.authenticator.util.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/* Parts of these commands classes were taken from
 * LielAmar's 2FA plugin
 */
public abstract class CommandWithSubCommands extends Command
{
    public CommandWithSubCommands(@NotNull String command, @Nullable String permission)
    {
        super(command, permission);
    }

    public abstract void subCommandNotFoundEvent(@NotNull CommandSender sender);
    public abstract @NotNull Command[] getSubCommands();

    public @Nullable Command getSubCommand(@NotNull String name)
    {
        for(Command subCommand : getSubCommands())
        {
            if(subCommand.getCommand().equalsIgnoreCase(name))
                return subCommand;

            if(subCommand.getAliases() != null)
            {
                for(String alias : subCommand.getAliases())
                {
                    if(alias.equalsIgnoreCase(name))
                        return subCommand;
                }
            }
        }
        return null;
    }
}
