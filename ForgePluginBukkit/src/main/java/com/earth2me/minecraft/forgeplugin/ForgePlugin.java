package com.earth2me.minecraft.forgeplugin;

import com.earth2me.minecraft.forgeplugin.commands.ChunkCleanupCommand;
import com.earth2me.minecraft.forgeplugin.commands.ForgePluginCommand;
import com.earth2me.minecraft.forgeplugin.commands.IsPersistentCommand;
import com.earth2me.minecraft.forgeplugin.events.*;
import com.earth2me.minecraft.forgeplugin.patch.IPatch;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class ForgePlugin extends JavaPlugin
{
	@Getter
	private transient ItemMonitor itemMonitor;
	@Getter
	private transient ChunkManager chunkManager;
	@Getter
	private transient EventLogger eventLogger;
	@Getter
	private transient EntityOverflow entityOverflow;
	@Getter
	private transient VaultPermissionHandler vault;
	private transient Set<IEventHandler> eventHandlers;
	private transient Set<IPatch> patches;
	private transient Set<IComponent> components;

	@Override
	public void onEnable()
	{
		eventHandlers = Sets.newHashSet(new IEventHandler[]
		{
			eventLogger = new EventLogger(this),
			itemMonitor = new ItemMonitor(this),
			entityOverflow = new EntityOverflow(this),
			chunkManager = new ChunkManager(this),
		});
		
		patches = Sets.newHashSet(new IPatch[]
		{
		});
		
		components = new HashSet<>();
		components.addAll(eventHandlers);
		components.addAll(patches);

		reload();

		final PluginManager pluginManager = getServer().getPluginManager();
		for (final IEventHandler eventHandler : eventHandlers)
		{
			try
			{
				pluginManager.registerEvents(eventHandler, this);
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to register an event handler as a listener with Bukkit.", ex);
			}
		}

		getCommand("forgeplugin").setExecutor(new ForgePluginCommand(this));
		getCommand("chunkcleanup").setExecutor(new ChunkCleanupCommand(this));
		getCommand("ispersistent").setExecutor(new IsPersistentCommand(this));

		super.onEnable();

		for (final IComponent component : components)
		{
			try
			{
				component.onEnabled();
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to cleanly enable a component.", ex);
			}
		}
	}

	public boolean reload()
	{
		boolean success = true;

		try
		{
			vault = new VaultPermissionHandler(getServer());
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.WARNING, "Vault not found.  You should probably get that.");
		}

		try
		{
			if (!new File(getDataFolder(), "config.yml").exists())
			{
				saveDefaultConfig();
			}

			reloadConfig();
			getConfig().options().pathSeparator('/');
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.SEVERE, "Unable to reload configuration.", ex);
			success = false;
		}
		
		for (final IComponent component : components)
		{
			try
			{
				component.onReload();
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to reload a component.", ex);
				success = false;
			}
		}

		return success;
	}

	public boolean checkPermission(final CommandSender sender, final Command command, final String subNode)
	{
		if (sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			return true;
		}

		final String node = "forgeplugin." + subNode;
		if (vault == null)
		{
			if (sender.hasPermission(node))
			{
				return true;
			}
		}
		else if (vault.hasPermission(sender, node))
		{
			return true;
		}

		sender.sendMessage(String.format("%sYou require the %s permission node to perform that command.", ChatColor.RED, node));
		return false;
	}
}
