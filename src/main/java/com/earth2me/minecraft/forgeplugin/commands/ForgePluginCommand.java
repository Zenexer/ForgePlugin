package com.earth2me.minecraft.forgeplugin.commands;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;


public final class ForgePluginCommand implements CommandExecutor
{
	private final transient ForgePlugin plugin;

	public ForgePluginCommand(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		if (!plugin.checkPermission(sender, command, "forgeplugin"))
		{
			return true;
		}
		
		if (args == null || args.length < 1)
		{
			sender.sendMessage(ChatColor.RED + "Expected a sub-command.");
			return false;
		}

		final String subcommand = args[0].toLowerCase(Locale.US);
		switch (subcommand)
		{
		case "r":
		case "reload":
			onReload(command, sender);
			return true;

		case "v":
		case "version":
			onVersion(command, sender);
			return true;

		default:
			sender.sendMessage(String.format("%s\"%s\" is not a known sub-command.", ChatColor.RED, subcommand));
			return false;
		}
	}

	private void onReload(final Command command, final CommandSender sender)
	{
		if (!plugin.checkPermission(sender, command, "forgeplugin.reload"))
		{
			return;
		}
		
		if (plugin.reload())
		{
			sender.sendMessage(ChatColor.GREEN + "Plugin reloaded successfully.");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Unable to reload configuration.  Check the YAML syntax.");
		}
	}

	private void onVersion(final Command command, final CommandSender sender)
	{
		if (!plugin.checkPermission(sender, command, "forgeplugin.version"))
		{
			return;
		}
		
		final PluginDescriptionFile description = plugin.getDescription();
		
		final String[] authorsArray = description.getAuthors().toArray(new String[0]);
		final StringBuilder authors = new StringBuilder();
		if (authorsArray.length > 0)
		{
			authors.append(authorsArray[0]);
			for (int i = 1; i < authorsArray.length; i++)
			{
				authors.append(", ").append(authorsArray[i]);
			}
		}
		
		final String color = ChatColor.LIGHT_PURPLE.toString();
		
		sender.sendMessage(new String[]
			{
				String.format("%s%s version %s", color, plugin.getName(), description.getVersion()),
				String.format("%sby %s", color, authors),
				String.format("%sURL: %s", color, description.getWebsite()),
			});
	}
}
