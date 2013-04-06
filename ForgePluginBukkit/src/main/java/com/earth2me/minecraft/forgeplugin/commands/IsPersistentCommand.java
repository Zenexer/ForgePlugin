package com.earth2me.minecraft.forgeplugin.commands;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public final class IsPersistentCommand implements CommandExecutor
{
	private final transient ForgePlugin plugin;
	
	public IsPersistentCommand(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		if (!plugin.checkPermission(sender, command, "ispersistent"))
		{
			return true;
		}
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "Only in-game players can use that command.");
			return true;
		}
		
		final Player player = (Player)sender;
		final Chunk chunk = player.getLocation().getChunk();
		
		final String state;
		final ChatColor color;
		if (plugin.getChunkManager().isPersistent(chunk))
		{
			color = ChatColor.GREEN;
			state = "is";
		}
		else
		{
			color = ChatColor.RED;
			state = "is not";
		}
		
		player.sendMessage(String.format("%sChunk (%d, %d) %s persistent.", color, chunk.getX(), chunk.getZ(), state));
		return true;
	}
}
