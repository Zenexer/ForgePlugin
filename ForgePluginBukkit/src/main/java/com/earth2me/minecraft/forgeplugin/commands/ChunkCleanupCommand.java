package com.earth2me.minecraft.forgeplugin.commands;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import com.earth2me.minecraft.forgeplugin.events.ChunkManager.Cleaner;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public final class ChunkCleanupCommand implements CommandExecutor
{
	private final transient ForgePlugin plugin;
	private final static transient ChatColor NORMAL_COLOR = ChatColor.LIGHT_PURPLE;
	private final static transient ChatColor FAIL_COLOR = ChatColor.RED;

	public ChunkCleanupCommand(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		if (!plugin.checkPermission(sender, command, "chunkcleanup"))
		{
			return false;
		}

		final Server server = plugin.getServer();
		final List<World> worlds = server.getWorlds();

		try
		{
			final Cleaner cleaner = plugin.getChunkManager().createCleaner();
			cleaner.run();
			final int count = cleaner.getCount();
			final int failed = cleaner.getFailed();

			final String format;
			if (failed <= 0)
			{
				format = "%1$sScanning %3$d chunks passively.  TPS may drop temporarily.";
			}
			else if (count > 0)
			{
				format = "%1$sScanning %3$d chunks passively.  %2$sFailed to schedule %4$d chunks for scanning.";
			}
			else
			{
				format = "%2$sFailed to schedule %4$d chunks for scanning.";
			}

			server.broadcastMessage(String.format(format, NORMAL_COLOR, FAIL_COLOR, count, failed));
		}
		catch (Throwable ex)
		{
			server.broadcastMessage(FAIL_COLOR + "Cleanup failed.");
			plugin.getLogger().log(Level.SEVERE, "Unable to schedule chunk cleanup.", ex);
		}

		return true;
	}
}
