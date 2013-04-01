package com.earth2me.minecraft.forgeplugin;

import com.earth2me.minecraft.forgeplugin.commands.ChunkCleanupCommand;
import com.earth2me.minecraft.forgeplugin.commands.ForgePluginCommand;
import com.earth2me.minecraft.forgeplugin.events.ChunkManager;
import com.earth2me.minecraft.forgeplugin.events.EventLogger;
import com.earth2me.minecraft.forgeplugin.events.ItemMonitor;
import java.io.File;
import java.util.HashSet;
import java.util.List;
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
	private final transient ItemMonitor itemMonitor = new ItemMonitor();
	@Getter
	private transient ChunkManager chunkManager;
	@Getter
	private transient EventLogger eventLogger;
	private transient VaultPermissionHandler vault;

	@Override
	public void onEnable()
	{
		eventLogger = new EventLogger(this);
		chunkManager = new ChunkManager(this);
		
		reload();
		
		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(eventLogger, this);
		pluginManager.registerEvents(chunkManager, this);
		pluginManager.registerEvents(itemMonitor, this);
		
		getCommand("forgeplugin").setExecutor(new ForgePluginCommand(this));
		getCommand("chunkcleanup").setExecutor(new ChunkCleanupCommand(this));

		super.onEnable();
		
		chunkManager.onEnabled();
	}

	private Set<ItemData> getItemSet(final String path)
	{
		final List<?> itemList = getConfig().getList(path);
		final Set<ItemData> items = new HashSet<>();

		if (itemList != null && !itemList.isEmpty())
		{
			for (Object i : itemList)
			{
				if (i == null)
				{
					continue;
				}

				final String[] tokens = i.toString().split(" ", 2);
				if (tokens.length < 1)
				{
					continue;
				}

				final ItemData item = ItemData.parse(tokens[0]);
				if (item != null)
				{
					items.add(item);
				}
			}
		}

		return items;
	}

	public boolean reload()
	{
		try
		{
			vault = new VaultPermissionHandler(getServer());
		}
		catch (Throwable ex)
		{
			getLogger().log(Level.WARNING, "Vault not found.");
		}

		try
		{
			if (!new File(getDataFolder(), "config.yml").exists())
			{
				saveDefaultConfig();
			}
			
			reloadConfig();
			getConfig().options().pathSeparator('/');

			itemMonitor.setBannedAll(getItemSet("banned-items/all"));
			itemMonitor.setBannedUse(getItemSet("banned-items/use"));
			itemMonitor.setBannedCraft(getItemSet("banned-items/craft"));
			itemMonitor.setBannedBuild(getItemSet("banned-items/build"));
		}
		catch (Throwable ex)
		{
			getLogger().log(Level.SEVERE, "Unable to reload configuration.", ex);
			return false;
		}
		
		try
		{
			chunkManager.reload();
		}
		catch (Throwable ex)
		{
			getLogger().log(Level.SEVERE, "Unable to reload chunk cleaner.", ex);
			return false;
		}
		
		return true;
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
