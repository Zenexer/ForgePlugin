package com.earth2me.minecraft.forgeplugin.commands;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public final class ChunkCleanupCommand implements CommandExecutor
{
	private final transient ForgePlugin plugin;

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

		plugin.getChunkManager().createCleaner().run();

		return true;
	}
}
